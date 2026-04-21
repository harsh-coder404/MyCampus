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



// testing backend running - cmd /c "netstat -ano | findstr :8081"

// API check should return 200 - Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:8081/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"proff@abc.com","password":"Proff@12","role":"PROFESSOR"}'

// backend start -
/*  Set-Location "D:\MinorApp\backend"
    $env:DB_USERNAME="postgres"
    $env:DB_PASSWORD="HarshAdmin"
    $env:SERVER_ADDRESS="0.0.0.0"
    $env:SERVER_PORT="8081"
    .\mvnw.cmd spring-boot:run
*/

// stop backend - 1 -> Ctrl + C in backend terminal
/*  $pid = (Get-NetTCPConnection -State Listen -LocalPort 8081).OwningProcess
    Stop-Process -Id $pid -Force
*/