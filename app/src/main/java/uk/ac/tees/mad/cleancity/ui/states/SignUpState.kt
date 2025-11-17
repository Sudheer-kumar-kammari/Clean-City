package uk.ac.tees.mad.cleancity.ui.states

sealed class SignUpUiState{
    object Idle: SignUpUiState()
    object Loading: SignUpUiState()
    data class Success(val userId:String): SignUpUiState()
    data class Error(val message:String): SignUpUiState()
}



data class SignUpFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false
)
