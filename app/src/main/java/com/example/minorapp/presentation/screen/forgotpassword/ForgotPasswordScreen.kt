package com.example.minorapp.presentation.screen.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.minorapp.ui.theme.AppTextStyles
import com.example.minorapp.ui.theme.MinorAppTheme

@Composable
fun ForgotPasswordScreen(
    email: String,
    onEmailChanged: (String) -> Unit,
    rollNumber: String,
    onRollNumberChanged: (String) -> Unit,
    message: String?,
    isVerifying: Boolean,
    onVerifyIdentityClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val background = Color(0xFFF2F4F8)
    val cardBackground = Color(0xFFF8F9FB)
    val primaryBlue = Color(0xFF0D5CAB)
    val sectionLabel = Color(0xFF667085)
    val mutedText = Color(0xFF667085)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 22.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = primaryBlue
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "MyCampus",
                style = AppTextStyles.screenTitle,
                color = primaryBlue
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .background(cardBackground, RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onBackClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    tint = primaryBlue
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Back",
                    style = AppTextStyles.primaryButton,
                    color = primaryBlue
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Forgot Password?",
                style = AppTextStyles.screenTitle.copy(fontSize = AppTextStyles.splashBrandTitle.fontSize),
                color = Color(0xFF101828)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enter your credentials to verify your identity. We will send a secure password reset link to your institutional inbox.",
                style = AppTextStyles.screenSubtitle,
                color = Color(0xFF344054)
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionLabel(text = "INSTITUTIONAL EMAIL", color = sectionLabel)
            UnderlineInput(
                value = email,
                onValueChange = onEmailChanged,
                placeholder = "e.g. j.doe@university.edu",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = mutedText
                    )
                },
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel(text = "ROLL NUMBER", color = sectionLabel)
            UnderlineInput(
                value = rollNumber,
                onValueChange = onRollNumberChanged,
                placeholder = "e.g. 2024-CSC-042",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Badge,
                        contentDescription = null,
                        tint = mutedText
                    )
                }
            )

            if (message != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = message,
                    style = AppTextStyles.body,
                    color = if (message.startsWith("Identity verified")) Color(0xFF027A48) else Color(0xFFB42318)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onVerifyIdentityClick,
                enabled = !isVerifying,
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isVerifying) "Verifying..." else "Verify Identity",
                        style = AppTextStyles.primaryButton,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFE4E7EC))
            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Help,
                        contentDescription = null,
                        tint = primaryBlue
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Having trouble?",
                        style = AppTextStyles.primaryButton,
                        color = Color(0xFF101828)
                    )
                    Text(
                        text = "Contact the university IT Helpdesk for manual recovery.",
                        style = AppTextStyles.screenSubtitle,
                        color = Color(0xFF344054)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ACADEMIC UTILITY SYSTEM © 2024",
                style = AppTextStyles.footer,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        style = AppTextStyles.sectionLabel
    )
}

@Composable
private fun UnderlineInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon()
            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = AppTextStyles.screenSubtitle.copy(color = Color(0xFF344054)),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = AppTextStyles.screenSubtitle,
                            color = Color(0xFF98A2B3)
                        )
                    }
                    innerTextField()
                }
            )
        }
        HorizontalDivider(color = Color(0xFFB8C0CE), thickness = 2.dp)
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    MinorAppTheme {
        ForgotPasswordScreen(
            email = "",
            onEmailChanged = {},
            rollNumber = "",
            onRollNumberChanged = {},
            message = null,
            isVerifying = false,
            onVerifyIdentityClick = {},
            onBackClick = {}
        )
    }
}
