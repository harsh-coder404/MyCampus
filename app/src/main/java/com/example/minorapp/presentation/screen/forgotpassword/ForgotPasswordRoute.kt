package com.example.minorapp.presentation.screen.forgotpassword

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.BuildConfig
import com.example.minorapp.data.auth.ForgotPasswordRepository

@Composable
fun ForgotPasswordRoute(
    onNavigateToResetPassword: () -> Unit,
    onNavigateToResetLinkSent: (String) -> Unit = {},
    onBackToLoginClick: () -> Unit
) {
    val repository = remember { ForgotPasswordRepository(BuildConfig.AUTH_BASE_URL) }
    val viewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModel.factory(repository)
    )
    val uiState = viewModel.uiState

    ForgotPasswordScreen(
        email = uiState.email,
        onEmailChanged = viewModel::onEmailChanged,
        rollNumber = uiState.rollNumber,
        onRollNumberChanged = viewModel::onRollNumberChanged,
        message = uiState.message,
        isVerifying = uiState.isVerifying,
        onVerifyIdentityClick = {
            viewModel.onVerifyIdentityClick(
                onSuccess = { _ -> onNavigateToResetPassword() }
            )
        },
        onBackClick = onBackToLoginClick
    )
}
