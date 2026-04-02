package com.example.minorapp.presentation.screen.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.minorapp.data.session.SessionManager
import kotlinx.coroutines.delay

@Composable
fun SplashRoute(
    sessionManager: SessionManager,
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    SplashScreen()

    LaunchedEffect(Unit) {
        delay(1_000)
        if (sessionManager.shouldAutoLogin()) {
            onNavigateToDashboard()
        } else {
            onNavigateToLogin()
        }
    }
}
