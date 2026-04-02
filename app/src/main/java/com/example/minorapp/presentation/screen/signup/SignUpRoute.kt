package com.example.minorapp.presentation.screen.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.BuildConfig
import com.example.minorapp.data.auth.SignUpRepository

@Composable
fun SignUpRoute(
    onSignInClick: () -> Unit
) {
    val signUpRepository = remember { SignUpRepository(BuildConfig.AUTH_BASE_URL) }
    val viewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModel.factory(signUpRepository)
    )
    val uiState = viewModel.uiState

    SignUpScreen(
        selectedRole = uiState.selectedRole,
        onRoleSelected = viewModel::onRoleSelected,
        email = uiState.email,
        emailError = uiState.emailError,
        onEmailChanged = viewModel::onEmailChanged,
        rollNumber = uiState.rollNumber,
        onRollNumberChanged = viewModel::onRollNumberChanged,
        password = uiState.password,
        onPasswordChanged = viewModel::onPasswordChanged,
        confirmPassword = uiState.confirmPassword,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        showPassword = uiState.showPassword,
        onShowPasswordChange = viewModel::onShowPasswordChange,
        isRegistering = uiState.isRegistering,
        infoMessage = uiState.infoMessage,
        onCreateAccountClick = viewModel::onCreateAccountClick,
        onSignInClick = onSignInClick
    )
}






