package com.example.minorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.minorapp.presentation.app.MyCampusApp
import com.example.minorapp.presentation.navigation.AppRoute
import com.example.minorapp.ui.theme.MinorAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinorAppTheme {
                MyCampusApp(startDestination = AppRoute.Splash.route)
            }
        }
    }
}



