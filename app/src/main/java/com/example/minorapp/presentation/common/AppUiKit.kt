package com.example.minorapp.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AppBlueTheme {
    val ScreenBackground = Color(0xFFEAF4FF)
    val CardGradient = listOf(Color(0xFF2B86E8), Color(0xFF5FB6FF))
    val ButtonGradient = listOf(Color(0xFF1D74DB), Color(0xFF59B8FF))
    val NeutralButtonGradient = listOf(Color(0xFF64748B), Color(0xFF94A3B8))
    val CardBorder = Color(0x55FFFFFF)
    val DisabledButton = Color(0xFF93C5FD)
    val PrimaryText = Color(0xFF0F172A)
    val CardCornerRadius = 24.dp
    val CardShadow = 10.dp
    val ButtonCornerRadius = 24.dp
    val ButtonHeight = 56.dp
}

@Composable
fun BlueGradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    gradientColors: List<Color> = AppBlueTheme.CardGradient,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    val effectiveShape = RoundedCornerShape(AppBlueTheme.CardCornerRadius)
    val borderModifier = if (border != null) {
        Modifier.border(border = border, shape = effectiveShape)
    } else {
        Modifier.border(width = 1.dp, color = AppBlueTheme.CardBorder, shape = effectiveShape)
    }
    Box(
        modifier = modifier
            .shadow(AppBlueTheme.CardShadow, effectiveShape)
            .clip(effectiveShape)
            .background(brush = Brush.horizontalGradient(gradientColors))
            .then(borderModifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x12000000))
        ) {
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                androidx.compose.material3.ProvideTextStyle(TextStyle(color = Color.White)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun BlueGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(24.dp),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
    isNeutral: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val effectiveShape = RoundedCornerShape(AppBlueTheme.ButtonCornerRadius)
    val buttonBrush = if (enabled) {
        Brush.horizontalGradient(if (isNeutral) AppBlueTheme.NeutralButtonGradient else AppBlueTheme.ButtonGradient)
    } else {
        Brush.horizontalGradient(listOf(AppBlueTheme.DisabledButton, AppBlueTheme.DisabledButton))
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(AppBlueTheme.ButtonHeight)
            .shadow(if (elevation == null) 0.dp else 4.dp, effectiveShape),
        shape = effectiveShape,
        colors = colors,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(effectiveShape)
                .background(buttonBrush),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}










