package com.example.minorapp.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.minorapp.data.session.SessionManager
import androidx.navigation.compose.rememberNavController
import com.example.minorapp.presentation.navigation.AppRoute
import com.example.minorapp.presentation.navigation.MyCampusNavHost

@Composable
fun MyCampusApp(
    startDestination: String = AppRoute.Splash.route
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val navController = rememberNavController()
    MyCampusNavHost(
        navController = navController,
        sessionManager = sessionManager,
        startDestination = startDestination
    )
}
