package com.example.minorapp.presentation.screen.forgotpassword

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minorapp.data.auth.ForgotPasswordRepository
import com.example.minorapp.data.auth.ForgotPasswordResult
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val rollNumber: String = "",
    val emailError: String? = null,
    val message: String? = null,
    val isVerifying: Boolean = false
)

class ForgotPasswordViewModel(
    private val forgotPasswordRepository: ForgotPasswordRepository
) : ViewModel() {
    var uiState by mutableStateOf(ForgotPasswordUiState())
        private set

    companion object {
        fun factory(forgotPasswordRepository: ForgotPasswordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ForgotPasswordViewModel(forgotPasswordRepository) as T
                }
            }
        }
    }

    fun onEmailChanged(email: String) {
        val trimmedEmail = email.trim()
        uiState = uiState.copy(
            email = trimmedEmail,
            emailError = validateEmail(trimmedEmail),
            message = null
        )
    }

    fun onRollNumberChanged(rollNumber: String) {
        uiState = uiState.copy(rollNumber = rollNumber, message = null)
    }

    fun onVerifyIdentityClick(onSuccess: (String) -> Unit) {
        val validationError = validateInputs()
        if (validationError != null) {
            uiState = uiState.copy(message = validationError)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isVerifying = true,
                message = "Verifying identity..."
            )

            when (
                val result = forgotPasswordRepository.verifyIdentity(
                    email = uiState.email,
                    rollNumber = uiState.rollNumber
                )
            ) {
                is ForgotPasswordResult.Success -> {
                    uiState = uiState.copy(
                        isVerifying = false,
                        message = result.message
                    )
                    onSuccess(result.message)
                }

                is ForgotPasswordResult.Failure -> {
                    uiState = uiState.copy(
                        isVerifying = false,
                        message = result.message
                    )
                }
            }
        }
    }

    private fun validateInputs(): String? {
        return when {
            uiState.email.isBlank() || uiState.rollNumber.isBlank() -> {
                "Please enter your institutional email and roll number."
            }

            uiState.emailError != null -> uiState.emailError

            else -> null
        }
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return null
        return if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            null
        } else {
            "Please enter a valid institutional email address."
        }
    }
}


