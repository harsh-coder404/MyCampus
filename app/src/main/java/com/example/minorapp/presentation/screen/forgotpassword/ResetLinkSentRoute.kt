package com.example.minorapp.presentation.screen.forgotpassword

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun ResetLinkSentRoute(
    message: String,
    onNavigateToLogin: () -> Unit
) {
    ResetLinkSentScreen(message = message)

    LaunchedEffect(Unit) {
        delay(1_250)
        onNavigateToLogin()
    }
}

