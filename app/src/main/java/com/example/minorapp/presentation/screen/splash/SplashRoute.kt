package com.example.minorapp.presentation.screen.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.model.UserRole
import kotlinx.coroutines.delay

@Composable
fun SplashRoute(
    sessionManager: SessionManager,
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: (UserRole) -> Unit
) {
    SplashScreen()

    LaunchedEffect(Unit) {
        delay(1_000)
        if (sessionManager.shouldAutoLogin()) {
            onNavigateToDashboard(sessionManager.getSavedRole())
        } else {
            onNavigateToLogin()
        }
    }
}
