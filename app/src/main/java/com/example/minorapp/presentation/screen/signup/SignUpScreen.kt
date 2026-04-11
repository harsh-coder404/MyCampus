package com.example.minorapp.presentation.screen.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.minorapp.domain.model.UserRole
import com.example.minorapp.ui.theme.AppTextStyles
import com.example.minorapp.ui.theme.MinorAppTheme

@Composable
fun SignUpScreen(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    email: String,
    emailError: String?,
    onEmailChanged: (String) -> Unit,
    rollNumber: String,
    onRollNumberChanged: (String) -> Unit,
    password: String,
    onPasswordChanged: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChanged: (String) -> Unit,
    showPassword: Boolean,
    onShowPasswordChange: (Boolean) -> Unit,
    isRegistering: Boolean,
    infoMessage: String?,
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit
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
            .padding(horizontal = 22.dp, vertical = 40.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = primaryBlue
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "MyCampus",
                color = primaryBlue,
                style = AppTextStyles.screenTitle
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackground, RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Setup Your Profile",
                style = AppTextStyles.screenTitle,
                color = Color(0xFF101828)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select your role and enter your institutional details.",
                style = AppTextStyles.screenSubtitle,
                color = Color(0xFF344054)
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionLabel(text = "SELECT ROLE", color = sectionLabel)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F2F4), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RoleSegment(
                    title = "Student",
                    icon = { Icon(Icons.Outlined.School, contentDescription = null) },
                    isSelected = selectedRole == UserRole.STUDENT,
                    primaryBlue = primaryBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onRoleSelected(UserRole.STUDENT) }
                )
                RoleSegment(
                    title = "Professor",
                    icon = { Icon(Icons.Outlined.Badge, contentDescription = null) },
                    isSelected = selectedRole == UserRole.PROFESSOR,
                    primaryBlue = primaryBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onRoleSelected(UserRole.PROFESSOR) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel(text = "INSTITUTIONAL EMAIL", color = sectionLabel)
            UnderlineInput(
                value = email,
                onValueChange = onEmailChanged,
                placeholder = "name@university.edu",
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = mutedText) },
                keyboardType = KeyboardType.Email
            )

            if (emailError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = emailError,
                    style = AppTextStyles.body,
                    color = Color(0xFFB42318)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            SectionLabel(text = "ROLL NUMBER", color = sectionLabel)
            UnderlineInput(
                value = rollNumber,
                onValueChange = onRollNumberChanged,
                placeholder = "e.g. 2024-CS-123",
                leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = mutedText) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            SectionLabel(text = "PASSWORD", color = sectionLabel)
            UnderlineInput(
                value = password,
                onValueChange = onPasswordChanged,
                placeholder = "••••••••",
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = mutedText) },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = mutedText,
                        modifier = Modifier.clickable { onShowPasswordChange(!showPassword) }
                    )
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            SectionLabel(text = "CONFIRM PASSWORD", color = sectionLabel)
            UnderlineInput(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                placeholder = "••••••••",
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = mutedText) },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = mutedText,
                        modifier = Modifier.clickable { onShowPasswordChange(!showPassword) }
                    )
                }
            )

            if (infoMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = infoMessage,
                    style = AppTextStyles.body,
                    color = if (infoMessage.contains("ready")) Color(0xFF027A48) else Color(0xFFB42318)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCreateAccountClick,
                enabled = !isRegistering,
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Create Account",
                        style = AppTextStyles.primaryButton,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    style = AppTextStyles.body,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "Sign In",
                    style = AppTextStyles.smallLink,
                    color = primaryBlue,
                    modifier = Modifier.clickable(onClick = onSignInClick)
                )
            }
        }

        Spacer(modifier = Modifier.height(52.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.VerifiedUser,
                contentDescription = null,
                tint = Color(0xFF98A2B3)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "ENTERPRISE GRADE ACADEMIC SECURITY",
                style = AppTextStyles.footer,
                color = Color(0xFFB0B5BE)
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
private fun RoleSegment(
    title: String,
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    primaryBlue: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .background(
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) Color(0xFFD0D5DD) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = AppTextStyles.primaryButton,
            color = if (isSelected) primaryBlue else Color(0xFF344054)
        )
    }
}

@Composable
private fun UnderlineInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
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
                visualTransformation = visualTransformation,
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

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
        HorizontalDivider(color = Color(0xFFB8C0CE), thickness = 2.dp)
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    MinorAppTheme {
        SignUpScreen(
            selectedRole = UserRole.STUDENT,
            onRoleSelected = {},
            email = "",
            emailError = null,
            onEmailChanged = {},
            rollNumber = "",
            onRollNumberChanged = {},
            password = "",
            onPasswordChanged = {},
            confirmPassword = "",
            onConfirmPasswordChanged = {},
            showPassword = false,
            onShowPasswordChange = {},
            isRegistering = false,
            infoMessage = null,
            onCreateAccountClick = {},
            onSignInClick = {}
        )
    }
}



