package com.example.minorapp.presentation.screen.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.minorapp.ui.theme.AppTextStyles
import com.example.minorapp.ui.theme.MinorAppTheme

@Composable
fun ResetLinkSentScreen(message: String) {
    val background = Color(0xFFF2F4F8)
    val primaryBlue = Color(0xFF0D5CAB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.MarkEmailRead,
            contentDescription = null,
            tint = primaryBlue
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Reset Link Sent",
            style = AppTextStyles.screenTitle,
            color = Color(0xFF101828)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = message,
            style = AppTextStyles.screenSubtitle,
            color = Color(0xFF344054),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResetLinkSentScreenPreview() {
    MinorAppTheme {
        ResetLinkSentScreen(
            message = "A secure password reset link has been sent to your institutional inbox."
        )
    }
}

