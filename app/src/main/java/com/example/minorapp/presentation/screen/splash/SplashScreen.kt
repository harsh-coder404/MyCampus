package com.example.minorapp.presentation.screen.splash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.minorapp.presentation.common.AppBlueTheme
import com.example.minorapp.presentation.common.BlueGradientCard as Card
import com.example.minorapp.ui.theme.AppTextStyles
import com.example.minorapp.ui.theme.MinorAppTheme

@Composable
fun SplashScreen() {
    val backgroundColor = AppBlueTheme.ScreenBackground
    val primaryBlue = Color(0xFF0D5CAB)
    val secondaryText = Color(0xFF5F6E85)
    val trackColor = Color(0xFFD9DEE8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.9f))

            Card(shape = RoundedCornerShape(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    LogoPlaceholder(primaryBlue = primaryBlue)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "MyCampus",
                color = primaryBlue,
                style = AppTextStyles.splashBrandTitle
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "PRECISION ACADEMIC UTILITY",
                color = secondaryText,
                style = AppTextStyles.splashTagline,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(52.dp))

            Box(
                modifier = Modifier
                    .width(290.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(trackColor)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(68.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(primaryBlue)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "POWERED BY ACADEMIC INSIGHTS",
                color = secondaryText,
                style = AppTextStyles.splashMeta
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Dot(color = Color(0xFFC3D0E7))
                Dot(color = Color(0xFF7FA9DB))
                Dot(color = Color(0xFFC3D0E7))
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun LogoPlaceholder(primaryBlue: Color) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7FAFF))
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.border(
                BorderStroke(1.dp, primaryBlue.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(12.dp)
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color(0xFFF7FAFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "APP LOGO",
                    color = Color.White,
                    style = AppTextStyles.logoPlaceholder
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenPreview() {
    MinorAppTheme {
        SplashScreen()
    }
}


