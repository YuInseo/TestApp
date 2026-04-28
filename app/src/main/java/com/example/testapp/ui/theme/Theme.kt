package com.example.testapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.testapp.data.preferences.AccentPreset
import com.example.testapp.data.preferences.FontScale

object AppColors {
    val Accent = Color(0xFF3B82F6)
    val AccentSoft = Color(0xFF60A5FA)
    val Overdue = Color(0xFFEF4444)
    val Upcoming = Color(0xFF60A5FA)
    val Done = Color(0xFF6B7280)
    val DotIndicator = Color(0xFF3B82F6)
}

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Accent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color.White,
    tertiary = Color(0xFFFB923C),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFF9CA3AF),
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF242424),
    surfaceContainerHighest = Color(0xFF2C2C2E),
    surfaceContainerLow = Color(0xFF111111),
    outline = Color(0xFF3F3F46),
    outlineVariant = Color(0xFF27272A),
    error = AppColors.Overdue,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Accent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF64748B),
    onSecondary = Color.White,
    tertiary = Color(0xFFEA580C),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    surfaceContainer = Color(0xFFF8FAFC),
    surfaceContainerHigh = Color(0xFFF1F5F9),
    surfaceContainerHighest = Color(0xFFE2E8F0),
    surfaceContainerLow = Color(0xFFFFFFFF),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = AppColors.Overdue
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    accentPreset: AccentPreset = AccentPreset.DEFAULT,
    fontScale: FontScale = FontScale.NORMAL,
    content: @Composable () -> Unit
) {
    val accent = Color(accentPreset.argb)
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = baseScheme.copy(
        primary = accent,
        primaryContainer = accent.copy(alpha = 0.25f),
        onPrimaryContainer = if (darkTheme) Color.White else Color.Black
    )
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography(fontScale.factor),
        content = content
    )
}

private fun scaledTypography(scale: Float): Typography {
    if (scale == 1.0f) return Typography()
    val base = Typography()
    fun TextStyle.scaled(): TextStyle = copy(
        fontSize = fontSize.scale(scale),
        lineHeight = lineHeight.scale(scale)
    )
    return Typography(
        displayLarge = base.displayLarge.scaled(),
        displayMedium = base.displayMedium.scaled(),
        displaySmall = base.displaySmall.scaled(),
        headlineLarge = base.headlineLarge.scaled(),
        headlineMedium = base.headlineMedium.scaled(),
        headlineSmall = base.headlineSmall.scaled(),
        titleLarge = base.titleLarge.scaled(),
        titleMedium = base.titleMedium.scaled(),
        titleSmall = base.titleSmall.scaled(),
        bodyLarge = base.bodyLarge.scaled(),
        bodyMedium = base.bodyMedium.scaled(),
        bodySmall = base.bodySmall.scaled(),
        labelLarge = base.labelLarge.scaled(),
        labelMedium = base.labelMedium.scaled(),
        labelSmall = base.labelSmall.scaled()
    )
}

private fun TextUnit.scale(factor: Float): TextUnit =
    if (type == TextUnitType.Sp) (value * factor).sp else this
