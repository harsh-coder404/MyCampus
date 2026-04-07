package com.example.minorapp.presentation.screen.login

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minorapp.data.auth.AuthRepository
import com.example.minorapp.data.auth.AuthResult
import com.example.minorapp.domain.model.UserRole
import kotlinx.coroutines.launch

data class LoginUiState(
    val selectedRole: UserRole = UserRole.STUDENT,
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val errorMessage: String? = null,
    val authStatusMessage: String? = null,
    val isAuthenticating: Boolean = false,
    val credentialsVerified: Boolean = false,
    val authenticatedRole: UserRole? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val username: String? = null,
    val branch: String? = null,
    val batch: String? = null,
    val rememberFor30Days: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(authRepository) as T
                }
            }
        }
    }

    fun onRoleSelected(role: UserRole) {
        uiState = uiState.copy(
            selectedRole = role,
            credentialsVerified = false,
            authenticatedRole = null,
            accessToken = null,
            refreshToken = null,
            username = null,
            branch = null,
            batch = null,
            authStatusMessage = null,
            errorMessage = null
        )
    }

    fun onEmailChanged(email: String) {
        val trimmedEmail = email.trim()
        uiState = uiState.copy(
            email = trimmedEmail,
            emailError = validateEmail(trimmedEmail),
            errorMessage = null,
            authStatusMessage = null,
            credentialsVerified = false,
            authenticatedRole = null,
            accessToken = null,
            refreshToken = null,
            username = null,
            branch = null,
            batch = null
        )
    }

    fun onPasswordChanged(password: String) {
        uiState = uiState.copy(
            password = password,
            errorMessage = null,
            authStatusMessage = null,
            credentialsVerified = false,
            authenticatedRole = null,
            accessToken = null,
            refreshToken = null,
            username = null,
            branch = null,
            batch = null
        )
    }

    fun onRememberFor30DaysChanged(rememberFor30Days: Boolean) {
        uiState = uiState.copy(rememberFor30Days = rememberFor30Days)
    }


    fun onLoginRequested(onValidRequest: (LoginUiState) -> Unit) {
        if (uiState.email.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Please enter email and security key.",
                authStatusMessage = null
            )
            return
        }

        if (uiState.emailError != null) {
            uiState = uiState.copy(
                errorMessage = uiState.emailError,
                authStatusMessage = null
            )
            return
        }

        viewModelScope.launch {
            val authenticated = authenticate()
            if (authenticated) {
                onValidRequest(uiState)
            }
        }
    }

    private suspend fun authenticate(): Boolean {
        uiState = uiState.copy(
            isAuthenticating = true,
            errorMessage = null,
            authStatusMessage = null
        )

        return when (
            val result = authRepository.authenticate(
                email = uiState.email,
                password = uiState.password,
                selectedRole = uiState.selectedRole
            )
        ) {
            is AuthResult.Success -> {
                if (result.role != uiState.selectedRole) {
                    uiState = uiState.copy(
                        isAuthenticating = false,
                        credentialsVerified = false,
                        authStatusMessage = null,
                        errorMessage = "Selected role does not match this account.",
                        username = null,
                        branch = null,
                        batch = null
                    )
                    return false
                }
                uiState = uiState.copy(
                    isAuthenticating = false,
                    credentialsVerified = true,
                    authenticatedRole = result.role,
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    username = result.username,
                    branch = result.branch,
                    batch = result.batch,
                    errorMessage = null,
                    authStatusMessage = null
                )
                true
            }

            is AuthResult.Failure -> {
                val resolvedMessage = if (result.message.equals("Invalid email or security key.", ignoreCase = true)) {
                    "Wrong credentials"
                } else {
                    result.message
                }
                uiState = uiState.copy(
                    isAuthenticating = false,
                    credentialsVerified = false,
                    authStatusMessage = null,
                    errorMessage = resolvedMessage,
                    username = null,
                    branch = null,
                    batch = null
                )
                false
            }
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
