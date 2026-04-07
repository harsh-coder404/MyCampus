package com.example.minorapp.presentation.screen.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.BuildConfig
import com.example.minorapp.data.auth.AuthRepository
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.model.UserRole

@Composable
fun LoginRoute(
    sessionManager: SessionManager,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToDashboard: (UserRole) -> Unit
) {
    val authRepository = remember { AuthRepository(BuildConfig.AUTH_BASE_URL) }
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.factory(authRepository)
    )

    LoginScreen(
        uiState = viewModel.uiState,
        onRoleSelected = viewModel::onRoleSelected,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onRememberFor30DaysChanged = viewModel::onRememberFor30DaysChanged,
        onForgotPasswordClick = onNavigateToForgotPassword,
        onRequestAccessClick = onNavigateToSignUp,
        onLoginClick = {
            viewModel.onLoginRequested { state ->
                val authenticatedRole = state.authenticatedRole ?: return@onLoginRequested
                val accessToken = state.accessToken ?: return@onLoginRequested
                val refreshToken = state.refreshToken ?: return@onLoginRequested

                sessionManager.saveLogin(
                    role = authenticatedRole,
                    rememberFor30Days = state.rememberFor30Days,
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    email = state.email,
                    username = state.username,
                    branch = state.branch,
                    batch = state.batch
                )
                onNavigateToDashboard(authenticatedRole)
            }
        }
    )
}
