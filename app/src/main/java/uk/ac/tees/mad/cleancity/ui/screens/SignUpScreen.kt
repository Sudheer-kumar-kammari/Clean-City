package uk.ac.tees.mad.cleancity.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.cleancity.ui.states.SignUpUiState
import uk.ac.tees.mad.cleancity.viewmodel.AuthViewModel

// Colors - Eco-friendly Green Theme
private val GreenPrimary = Color(0xFF4CAF50)
private val GreenDark = Color(0xFF2E7D32)
private val GreenDarker = Color(0xFF1B5E20)
private val GreenLight = Color(0xFF81C784)
private val White = Color(0xFFFFFFFF)
private val Gray700 = Color(0xFF616161)
private val ErrorRed = Color(0xFFF44336)

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel ,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val signUpUiState by viewModel.signUpUiState.collectAsState()
    val signUpFormState by viewModel.signUpFormState.collectAsState()

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Handle navigation based on signup state
    LaunchedEffect(signUpUiState) {
        when (signUpUiState) {
            is SignUpUiState.Success -> {
                onNavigateToHome()
                viewModel.resetSignUpUiState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GreenPrimary.copy(alpha = 0.1f),
                        White,
                        GreenLight.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Icon(
                imageVector = Icons.Filled.Recycling,
                contentDescription = "CleanCity Logo",
                tint = GreenPrimary,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDarker
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Join us in making your city cleaner",
                fontSize = 16.sp,
                color = Gray700,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Name TextField
            OutlinedTextField(
                value = signUpFormState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Full Name") },
                placeholder = { Text("Enter your name") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Name Icon",
                        tint = GreenPrimary
                    )
                },
                isError = signUpFormState.nameError != null,
                supportingText = signUpFormState.nameError?.let {
                    { Text(it, color = ErrorRed) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Gray700.copy(alpha = 0.5f),
                    focusedLabelColor = GreenPrimary,
                    cursorColor = GreenPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email TextField
            OutlinedTextField(
                value = signUpFormState.email,
                onValueChange = viewModel::onSignUpEmailChange,
                label = { Text("Email") },
                placeholder = { Text("Enter your email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Email Icon",
                        tint = GreenPrimary
                    )
                },
                isError = signUpFormState.emailError != null,
                supportingText = signUpFormState.emailError?.let {
                    { Text(it, color = ErrorRed) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Gray700.copy(alpha = 0.5f),
                    focusedLabelColor = GreenPrimary,
                    cursorColor = GreenPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField
            OutlinedTextField(
                value = signUpFormState.password,
                onValueChange = viewModel::onSignUpPasswordChange,
                label = { Text("Password") },
                placeholder = { Text("Create a password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Password Icon",
                        tint = GreenPrimary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = viewModel::toggleSignUpPasswordVisibility) {
                        Icon(
                            imageVector = if (signUpFormState.isPasswordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = if (signUpFormState.isPasswordVisible)
                                "Hide password" else "Show password",
                            tint = Gray700
                        )
                    }
                },
                isError = signUpFormState.passwordError != null,
                supportingText = signUpFormState.passwordError?.let {
                    { Text(it, color = ErrorRed) }
                },
                visualTransformation = if (signUpFormState.isPasswordVisible)
                    VisualTransformation.None
                else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Gray700.copy(alpha = 0.5f),
                    focusedLabelColor = GreenPrimary,
                    cursorColor = GreenPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password TextField
            OutlinedTextField(
                value = signUpFormState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter your password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Confirm Password Icon",
                        tint = GreenPrimary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = viewModel::toggleConfirmPasswordVisibility) {
                        Icon(
                            imageVector = if (signUpFormState.isConfirmPasswordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = if (signUpFormState.isConfirmPasswordVisible)
                                "Hide password" else "Show password",
                            tint = Gray700
                        )
                    }
                },
                isError = signUpFormState.confirmPasswordError != null,
                supportingText = signUpFormState.confirmPasswordError?.let {
                    { Text(it, color = ErrorRed) }
                },
                visualTransformation = if (signUpFormState.isConfirmPasswordVisible)
                    VisualTransformation.None
                else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.signUp()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Gray700.copy(alpha = 0.5f),
                    focusedLabelColor = GreenPrimary,
                    cursorColor = GreenPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message from ViewModel
            if (signUpUiState is SignUpUiState.Error) {
                Text(
                    text = (signUpUiState as SignUpUiState.Error).message,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            // Sign Up Button
            Button(
                onClick = viewModel::signUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    disabledContainerColor = GreenPrimary.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = signUpUiState !is SignUpUiState.Loading
            ) {
                if (signUpUiState is SignUpUiState.Loading) {
                    CircularProgressIndicator(
                        color = White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account?",
                    color = Gray700,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    enabled = signUpUiState !is SignUpUiState.Loading
                ) {
                    Text(
                        text = "Login",
                        color = GreenPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


@Preview(showBackground = true, name = "CleanCity – Sign Up Screen")
@Composable
fun SignUpScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50).copy(alpha = 0.1f),
                        Color.White,
                        Color(0xFF81C784).copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Icon(
                imageVector = Icons.Filled.Recycling,
                contentDescription = "CleanCity Logo",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join us in making your city cleaner",
                fontSize = 16.sp,
                color = Color(0xFF616161),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Full Name
            OutlinedTextField(
                value = "Emma Green",
                onValueChange = {},
                label = { Text("Full Name") },
                placeholder = { Text("Enter your name") },
                leadingIcon = { Icon(Icons.Filled.Person, null, tint = Color(0xFF4CAF50)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF616161).copy(alpha = 0.5f),
                    focusedLabelColor = Color(0xFF4CAF50),
                    cursorColor = Color(0xFF4CAF50)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = "emma.green@example.com",
                onValueChange = {},
                label = { Text("Email") },
                placeholder = { Text("Enter your email") },
                leadingIcon = { Icon(Icons.Filled.Email, null, tint = Color(0xFF4CAF50)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF616161).copy(alpha = 0.5f),
                    focusedLabelColor = Color(0xFF4CAF50),
                    cursorColor = Color(0xFF4CAF50)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = "••••••••",
                onValueChange = {},
                label = { Text("Password") },
                placeholder = { Text("Create a password") },
                leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Color(0xFF4CAF50)) },
                trailingIcon = { Icon(Icons.Filled.VisibilityOff, null, tint = Color(0xFF616161)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF616161).copy(alpha = 0.5f),
                    focusedLabelColor = Color(0xFF4CAF50),
                    cursorColor = Color(0xFF4CAF50)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            OutlinedTextField(
                value = "••••••••",
                onValueChange = {},
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter your password") },
                leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Color(0xFF4CAF50)) },
                trailingIcon = { Icon(Icons.Filled.VisibilityOff, null, tint = Color(0xFF616161)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF616161).copy(alpha = 0.5f),
                    focusedLabelColor = Color(0xFF4CAF50),
                    cursorColor = Color(0xFF4CAF50)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Button
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account?", color = Color(0xFF616161))
                TextButton(onClick = {}) {
                    Text("Login", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}