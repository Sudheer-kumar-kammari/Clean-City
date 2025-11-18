package uk.ac.tees.mad.cleancity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.cleancity.ui.states.LoginFormState
import uk.ac.tees.mad.cleancity.ui.states.LoginUiState
import uk.ac.tees.mad.cleancity.ui.states.SignUpFormState
import uk.ac.tees.mad.cleancity.ui.states.SignUpUiState
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {


    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()
    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    // SignUp State
    private val _signUpUiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val signUpUiState: StateFlow<SignUpUiState> = _signUpUiState.asStateFlow()

    private val _signUpFormState = MutableStateFlow(SignUpFormState())
    val signUpFormState: StateFlow<SignUpFormState> = _signUpFormState.asStateFlow()


    fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null

    }


    fun onEmailChange(email: String) {
        _formState.value = _formState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun onPasswordChange(password: String) {
        _formState.value = _formState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun togglePasswordVisibility() {
        _formState.value = _formState.value.copy(
            isPasswordVisible = !_formState.value.isPasswordVisible
        )
    }

    fun login() {
        val currentFormState = _formState.value

        // Validate inputs
        if (!validateInputs(currentFormState)) {
            return
        }

        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading

            try {
                val result = firebaseAuth.signInWithEmailAndPassword(
                    currentFormState.email.trim(),
                    currentFormState.password
                ).await()

                val userId = result.user?.uid
                if (userId != null) {
                    _loginUiState.value = LoginUiState.Success(userId)
                } else {
                    _loginUiState.value = LoginUiState.Error("Login failed. Please try again.")
                }
            } catch (e: Exception) {
                _loginUiState.value = LoginUiState.Error(
                    getErrorMessage(e)
                )
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _loginUiState.value = LoginUiState.Error("Please enter your email address")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginUiState.value = LoginUiState.Error("Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading

            try {
                firebaseAuth.sendPasswordResetEmail(email.trim()).await()
                _loginUiState.value = LoginUiState.Success("Password reset email sent!")
            } catch (e: Exception) {
                _loginUiState.value = LoginUiState.Error(
                    "Failed to send reset email. Please try again."
                )
            }
        }
    }

    fun resetUiState() {
        _loginUiState.value = LoginUiState.Idle
    }

    private fun validateInputs(formState: LoginFormState): Boolean {
        var isValid = true

        // Validate email
        when {
            formState.email.isBlank() -> {
                _formState.value = formState.copy(emailError = "Email is required")
                isValid = false
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(formState.email).matches() -> {
                _formState.value = formState.copy(emailError = "Invalid email format")
                isValid = false
            }
        }

        // Validate password
        when {
            formState.password.isBlank() -> {
                _formState.value = formState.copy(passwordError = "Password is required")
                isValid = false
            }

            formState.password.length < 6 -> {
                _formState.value = formState.copy(
                    passwordError = "Password must be at least 6 characters"
                )
                isValid = false
            }
        }

        return isValid
    }

    private fun getErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("password is invalid") == true ->
                "Incorrect password. Please try again."

            exception.message?.contains("no user record") == true ->
                "No account found with this email."

            exception.message?.contains("network") == true ->
                "Network error. Please check your connection."

            exception.message?.contains("too many requests") == true ->
                "Too many attempts. Please try again later."

            else -> "Login failed. Please try again."
        }
    }

    // sign up functions

    fun onNameChange(name: String) {
        _signUpFormState.value = _signUpFormState.value.copy(
            name = name,
            nameError = null
        )
    }

    fun onSignUpEmailChange(email: String) {
        _signUpFormState.value = _signUpFormState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun onSignUpPasswordChange(password: String) {
        _signUpFormState.value = _signUpFormState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _signUpFormState.value = _signUpFormState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null
        )
    }

    fun toggleSignUpPasswordVisibility() {
        _signUpFormState.value = _signUpFormState.value.copy(
            isPasswordVisible = !_signUpFormState.value.isPasswordVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _signUpFormState.value = _signUpFormState.value.copy(
            isConfirmPasswordVisible = !_signUpFormState.value.isConfirmPasswordVisible
        )
    }

    fun signUp() {
        val currentFormState = _signUpFormState.value

        if (!validateSignUpInputs(currentFormState)) {
            return
        }

        viewModelScope.launch {
            _signUpUiState.value = SignUpUiState.Loading

            try {
                // Create user with email and password
                val result = firebaseAuth.createUserWithEmailAndPassword(
                    currentFormState.email.trim(),
                    currentFormState.password
                ).await()

                val user = result.user
                if (user != null) {
                    // Update user profile with name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(currentFormState.name.trim())
                        .build()

                    user.updateProfile(profileUpdates).await()

                    _signUpUiState.value = SignUpUiState.Success(user.uid)
                } else {
                    _signUpUiState.value = SignUpUiState.Error("Sign up failed. Please try again.")
                }
            } catch (e: Exception) {
                _signUpUiState.value = SignUpUiState.Error(getSignUpErrorMessage(e))
            }
        }
    }

    fun resetSignUpUiState() {
        _signUpUiState.value = SignUpUiState.Idle
    }

    private fun validateSignUpInputs(formState: SignUpFormState): Boolean {
        var isValid = true

        // Validate name
        when {
            formState.name.isBlank() -> {
                _signUpFormState.value = formState.copy(nameError = "Name is required")
                isValid = false
            }

            formState.name.length < 2 -> {
                _signUpFormState.value = formState.copy(
                    nameError = "Name must be at least 2 characters"
                )
                isValid = false
            }
        }

        // Validate email
        when {
            formState.email.isBlank() -> {
                _signUpFormState.value = formState.copy(emailError = "Email is required")
                isValid = false
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(formState.email).matches() -> {
                _signUpFormState.value = formState.copy(emailError = "Invalid email format")
                isValid = false
            }
        }

        // Validate password
        when {
            formState.password.isBlank() -> {
                _signUpFormState.value = formState.copy(passwordError = "Password is required")
                isValid = false
            }

            formState.password.length < 6 -> {
                _signUpFormState.value = formState.copy(
                    passwordError = "Password must be at least 6 characters"
                )
                isValid = false
            }
        }

        // Validate confirm password
        when {
            formState.confirmPassword.isBlank() -> {
                _signUpFormState.value = formState.copy(
                    confirmPasswordError = "Please confirm your password"
                )
                isValid = false
            }

            formState.password != formState.confirmPassword -> {
                _signUpFormState.value = formState.copy(
                    confirmPasswordError = "Passwords do not match"
                )
                isValid = false
            }
        }

        return isValid
    }


    private fun getSignUpErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("email address is already in use") == true ->
                "This email is already registered. Please login instead."

            exception.message?.contains("email address is badly formatted") == true ->
                "Invalid email format. Please check your email."

            exception.message?.contains("password is invalid") == true ->
                "Password must be at least 6 characters."

            exception.message?.contains("network") == true ->
                "Network error. Please check your connection."

            else -> "Sign up failed. Please try again."
        }
    }
}