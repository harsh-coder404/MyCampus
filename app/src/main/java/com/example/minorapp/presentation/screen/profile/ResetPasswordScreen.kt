package com.example.minorapp.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable

fun isPasswordValid(password: String): Boolean {
    return password.length >= 8 &&
            password.any { it.isLowerCase() } &&
            password.any { it.isUpperCase() } &&
            password.any { it.isDigit() } &&
            password.any { !it.isLetterOrDigit() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    requireCurrentPassword: Boolean = true,
    onBackClick: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    onPasswordUpdated: () -> Unit = {}
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val isCurrentPasswordError = requireCurrentPassword && currentPassword.isNotEmpty() && !isPasswordValid(currentPassword)
    val isNewPasswordError = newPassword.isNotEmpty() && !isPasswordValid(newPassword)
    val isConfirmPasswordError = confirmPassword.isNotEmpty() && confirmPassword != newPassword

    val hasValidCurrentPassword = !requireCurrentPassword || (currentPassword.isNotEmpty() && !isCurrentPasswordError)
    
    val isFormValid = hasValidCurrentPassword &&
            newPassword.isNotEmpty() && !isNewPasswordError &&
            confirmPassword.isNotEmpty() && !isConfirmPasswordError

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reset Password",
                        color = Color(0xFF0265DC),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(end = 48.dp),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0265DC)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Icon (Lock/Refresh style)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFDFEAFF),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh, // Placeholder for LockRefresh
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = Color(0xFF0265DC)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Security Update",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ensure your new password is at least 8 characters long and contains a mix of letters, numbers, and symbols.",
                color = Color(0xFF475569),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (requireCurrentPassword) {
                        PasswordField(
                            label = "CURRENT PASSWORD",
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            isVisible = isCurrentPasswordVisible,
                            onVisibilityChange = { isCurrentPasswordVisible = !isCurrentPasswordVisible },
                            isError = isCurrentPasswordError,
                            errorMessage = "Invalid password format",
                            actionText = "FORGOT?",
                            onActionClick = onNavigateToForgotPassword
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    PasswordField(
                        label = "NEW PASSWORD",
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        isVisible = isNewPasswordVisible,
                        onVisibilityChange = { isNewPasswordVisible = !isNewPasswordVisible },
                        isError = isNewPasswordError,
                        errorMessage = "Must contain 8+ chars, 1 uppercase, 1 lowercase, 1 number, 1 symbol"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PasswordField(
                        label = "RE-ENTER NEW PASSWORD",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        isVisible = isConfirmPasswordVisible,
                        onVisibilityChange = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                        isError = isConfirmPasswordError,
                        errorMessage = "Passwords do not match"
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onPasswordUpdated,
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0265DC),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF94A3B8),
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Update Password", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer Information Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDFEAFF),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Academic Integrity",
                            tint = Color(0xFF0265DC),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Academic Integrity",
                            color = Color(0xFF0265DC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your account security ensures the preservation of your academic milestones and research data.",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onVisibilityChange: () -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                color = if (isError) Color.Red else Color(0xFF334155),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            if (actionText != null && onActionClick != null) {
                Text(
                    text = actionText,
                    color = Color(0xFF0265DC),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.clickable { onActionClick() }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth(),
            border = if (isError) androidx.compose.foundation.BorderStroke(1.dp, Color.Red) else null
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    textStyle = TextStyle(
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 14.dp),
                    singleLine = true
                )
                IconButton(
                    onClick = onVisibilityChange,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Visibility",
                        tint = if (isError) Color.Red else Color(0xFF94A3B8)
                    )
                }
            }
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
