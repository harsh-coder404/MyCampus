package com.example.minorapp.presentation.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.minorapp.domain.model.UserRole
import com.example.minorapp.ui.theme.AppTextStyles
import com.example.minorapp.ui.theme.MinorAppTheme

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onRoleSelected: (UserRole) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRememberFor30DaysChanged: (Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRequestAccessClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val background = Color(0xFFF2F4F8)
    val cardBackground = Color(0xFFF8F9FB)
    val primaryBlue = Color(0xFF0D5CAB)
    val sectionLabel = Color(0xFF263143)
    val inputBackground = Color(0xFFE6E8EC)
    val mutedText = Color(0xFF6B7280)
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.equals("Wrong credentials", ignoreCase = true)) {
            Toast.makeText(context, "Wrong credentials", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackground, RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 26.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEAF2FF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = "App icon",
                        tint = primaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "MyCampus",
                    color = primaryBlue,
                    style = AppTextStyles.sectionLabel,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "Welcome Back",
                style = AppTextStyles.screenTitle,
                color = Color(0xFF111827),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Access your academic dashboard to\nmanage attendance and tasks.",
                style = AppTextStyles.screenSubtitle,
                color = Color(0xFF374151),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionLabel(text = "INSTITUTIONAL EMAIL", color = sectionLabel)
            Spacer(modifier = Modifier.height(10.dp))
            InputField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                placeholder = "scholar@university.edu",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = mutedText
                    )
                },
                keyboardType = KeyboardType.Email,
                visualTransformation = null,
                background = inputBackground
            )

            if (uiState.emailError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = uiState.emailError,
                    color = Color(0xFFB42318),
                    style = AppTextStyles.body
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionLabel(text = "SECURITY KEY", color = sectionLabel)
                Text(
                    text = "FORGOT?",
                    color = primaryBlue,
                    style = AppTextStyles.sectionLabel,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.clickable(onClick = onForgotPasswordClick)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            InputField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                placeholder = "••••••••",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = mutedText
                    )
                },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = mutedText
                        )
                    }
                },
                background = inputBackground
            )

            Spacer(modifier = Modifier.height(22.dp))

            SectionLabel(text = "IDENTIFY YOUR ROLE", color = sectionLabel)
            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RoleButton(
                    title = "Student",
                    isSelected = uiState.selectedRole == UserRole.STUDENT,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = null
                        )
                    },
                    onClick = { onRoleSelected(UserRole.STUDENT) },
                    modifier = Modifier.weight(1f),
                    primaryBlue = primaryBlue
                )

                RoleButton(
                    title = "Professor",
                    isSelected = uiState.selectedRole == UserRole.PROFESSOR,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Badge,
                            contentDescription = null
                        )
                    },
                    onClick = { onRoleSelected(UserRole.PROFESSOR) },
                    modifier = Modifier.weight(1f),
                    primaryBlue = primaryBlue
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRememberFor30DaysChanged(!uiState.rememberFor30Days) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.rememberFor30Days,
                    onCheckedChange = onRememberFor30DaysChanged
                )
                Text(
                    text = "Remember me for 30 days",
                    style = AppTextStyles.body,
                    color = Color(0xFF2C3646)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onLoginClick,
                enabled = !uiState.isAuthenticating,
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (uiState.isAuthenticating) "Authenticating..." else "Access Dashboard",
                        style = AppTextStyles.primaryButton,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage,
                    color = Color(0xFFB42318),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(26.dp))
            HorizontalDivider(color = Color(0xFFE4E7EC))
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "New here? ",
                    color = Color(0xFF1F2937),
                    style = AppTextStyles.smallLink.copy(fontWeight = FontWeight.Normal)
                )
                Text(
                    text = "Register your account",
                    color = primaryBlue,
                    style = AppTextStyles.smallLink,
                    modifier = Modifier.clickable(onClick = onRequestAccessClick)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        FooterInfoLine(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = mutedText
                )
            },
            text = "SECURE ACADEMIC PORTAL",
            color = mutedText
        )

        Spacer(modifier = Modifier.height(12.dp))

        FooterInfoLine(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Help,
                    contentDescription = null,
                    tint = mutedText
                )
            },
            text = "SUPPORT",
            color = mutedText
        )
        Spacer(modifier = Modifier.height(8.dp))
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
private fun RoleButton(
    title: String,
    isSelected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
    primaryBlue: Color
) {
    val selectedBackground = Color(0xFFEAF2FF)
    val defaultBackground = Color(0xFFF1F2F4)
    BoxWithConstraints(modifier = modifier) {
        val roleFontSize = adaptiveRoleFontSize(maxWidth)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .background(
                    color = if (isSelected) selectedBackground else defaultBackground,
                    shape = RoundedCornerShape(14.dp)
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) primaryBlue else Color(0xFFD8DBE2),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box {
                icon()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = roleFontSize,
                lineHeight = (roleFontSize.value + 4).sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) primaryBlue else Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun adaptiveRoleFontSize(maxWidth: Dp) = when {
    maxWidth < 150.dp -> 12.sp
    maxWidth < 170.dp -> 13.sp
    else -> 14.sp
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation?,
    trailingIcon: @Composable (() -> Unit)? = null,
    background: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = AppTextStyles.fieldText.fontSize,
            lineHeight = AppTextStyles.fieldText.lineHeight,
            fontWeight = AppTextStyles.fieldText.fontWeight
        ),
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF8E95A3),
                style = AppTextStyles.fieldText
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation
            ?: VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = background,
            unfocusedContainerColor = background,
            disabledContainerColor = background,
            focusedTextColor = Color(0xFF111827),
            unfocusedTextColor = Color(0xFF111827),
            disabledTextColor = Color(0xFF6B7280),
            cursorColor = Color(0xFF0D5CAB),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun FooterInfoLine(
    icon: @Composable () -> Unit,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = color,
            style = AppTextStyles.footer,
            textAlign = TextAlign.Center,
            letterSpacing = 1.8.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MinorAppTheme {
        LoginScreen(
            uiState = LoginUiState(),
            onRoleSelected = {},
            onEmailChanged = {},
            onPasswordChanged = {},
            onRememberFor30DaysChanged = {},
            onForgotPasswordClick = {},
            onRequestAccessClick = {},
            onLoginClick = {}
        )
    }
}



