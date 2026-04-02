package com.example.minorapp.presentation.screen.signup

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minorapp.data.auth.SignUpRepository
import com.example.minorapp.data.auth.SignUpResult
import com.example.minorapp.domain.model.UserRole
import kotlinx.coroutines.launch

data class SignUpUiState(
    val selectedRole: UserRole = UserRole.STUDENT,
    val email: String = "",
    val emailError: String? = null,
    val rollNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val showPassword: Boolean = false,
    val isRegistering: Boolean = false,
    val infoMessage: String? = null
)

class SignUpViewModel(
    private val signUpRepository: SignUpRepository
) : ViewModel() {
    var uiState by mutableStateOf(SignUpUiState())
        private set

    companion object {
        fun factory(signUpRepository: SignUpRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SignUpViewModel(signUpRepository) as T
                }
            }
        }
    }

    fun onRoleSelected(role: UserRole) {
        uiState = uiState.copy(selectedRole = role, infoMessage = null)
    }

    fun onEmailChanged(email: String) {
        val trimmedEmail = email.trim()
        uiState = uiState.copy(
            email = trimmedEmail,
            emailError = validateEmail(trimmedEmail),
            infoMessage = null
        )
    }

    fun onRollNumberChanged(rollNumber: String) {
        uiState = uiState.copy(rollNumber = rollNumber, infoMessage = null)
    }

    fun onPasswordChanged(password: String) {
        uiState = uiState.copy(password = password, infoMessage = null)
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        uiState = uiState.copy(confirmPassword = confirmPassword, infoMessage = null)
    }

    fun onShowPasswordChange(showPassword: Boolean) {
        uiState = uiState.copy(showPassword = showPassword)
    }

    fun onCreateAccountClick() {
        val validationError = validateInputs()
        if (validationError != null) {
            uiState = uiState.copy(infoMessage = validationError)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isRegistering = true,
                infoMessage = "Creating account..."
            )
            when (
                val result = signUpRepository.register(
                    email = uiState.email,
                    password = uiState.password,
                    rollNumber = uiState.rollNumber,
                    role = uiState.selectedRole
                )
            ) {
                is SignUpResult.Success -> {
                    uiState = uiState.copy(
                        isRegistering = false,
                        infoMessage = result.message
                    )
                }

                is SignUpResult.Failure -> {
                    uiState = uiState.copy(
                        isRegistering = false,
                        infoMessage = result.message
                    )
                }
            }
        }
    }

    private fun validateInputs(): String? {
        return when {
            uiState.email.isBlank() ||
                uiState.rollNumber.isBlank() ||
                uiState.password.isBlank() ||
                uiState.confirmPassword.isBlank() -> {
                "Please fill in all required fields."
            }

            uiState.emailError != null -> {
                uiState.emailError
            }

            uiState.password != uiState.confirmPassword -> {
                "Password and confirm password must match."
            }

            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return null
        return if (isValidEmail(email)) null else "Enter a valid institutional email."
    }
}





