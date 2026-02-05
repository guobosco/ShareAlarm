package com.example.sharealarm.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 应用主题
 * 功能：定义应用的主题配置，包括颜色、字体和形状
 * @param darkTheme 是否使用暗色主题
 * @param content 主题内容
 */
@Composable
fun ShareAlarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themeColor by ThemeSettings.themeColor.collectAsState()

    // 选择颜色方案
    val colorScheme = if (darkTheme) {
        // 暗色主题
        when (themeColor) {
            AppThemeColor.Yellow -> BuddyBellDarkScheme
            AppThemeColor.Blue -> darkColorScheme(
                primary = ThemeBlueDark,
                onPrimary = White,
                primaryContainer = ThemeBlueDark,
                onPrimaryContainer = White,
                background = Color(0xFF000000),
                onBackground = White,
                surface = Color(0xFF1C1C1E),
                onSurface = White
            )
            AppThemeColor.Green -> darkColorScheme(
                primary = ThemeGreenDark,
                onPrimary = White,
                primaryContainer = ThemeGreenDark,
                onPrimaryContainer = White,
                background = Color(0xFF000000),
                onBackground = White,
                surface = Color(0xFF1C1C1E),
                onSurface = White
            )
            AppThemeColor.Purple -> darkColorScheme(
                primary = ThemePurpleDark,
                onPrimary = White,
                primaryContainer = ThemePurpleDark,
                onPrimaryContainer = White,
                background = Color(0xFF000000),
                onBackground = White,
                surface = Color(0xFF1C1C1E),
                onSurface = White
            )
            AppThemeColor.Orange -> darkColorScheme(
                primary = ThemeOrange,
                onPrimary = White,
                primaryContainer = ThemeOrange,
                onPrimaryContainer = White,
                background = Color(0xFF000000),
                onBackground = White,
                surface = Color(0xFF1C1C1E),
                onSurface = White
            )
            AppThemeColor.Pink -> darkColorScheme(
                primary = ThemePink,
                onPrimary = White,
                primaryContainer = ThemePink,
                onPrimaryContainer = White,
                background = Color(0xFF000000),
                onBackground = White,
                surface = Color(0xFF1C1C1E),
                onSurface = White
            )
        }
    } else {
        // 亮色主题
        when (themeColor) {
            AppThemeColor.Yellow -> BuddyBellLightScheme
            AppThemeColor.Blue -> lightColorScheme(
                primary = ThemeBlue,
                onPrimary = White,
                primaryContainer = ThemeBlueLight,
                onPrimaryContainer = White,
                secondary = ThemeBlue,
                onSecondary = White,
                secondaryContainer = ThemeBlueLight,
                onSecondaryContainer = ThemeBlueDark,
                tertiary = ThemeBlue,
                onTertiary = White,
                background = IOSBackground,
                onBackground = IOSBlack,
                surface = IOSSurface,
                onSurface = IOSBlack,
                surfaceVariant = IOSLightGray,
                onSurfaceVariant = IOSGrayText,
                error = ErrorRed,
                onError = White,
                outline = IOSLightGray,
                outlineVariant = IOSGrayText
            )
            AppThemeColor.Green -> lightColorScheme(
                primary = ThemeGreen,
                onPrimary = White,
                primaryContainer = ThemeGreenLight,
                onPrimaryContainer = White,
                secondary = ThemeGreen,
                onSecondary = White,
                secondaryContainer = ThemeGreenLight,
                onSecondaryContainer = ThemeGreenDark,
                tertiary = ThemeGreen,
                onTertiary = White,
                background = IOSBackground,
                onBackground = IOSBlack,
                surface = IOSSurface,
                onSurface = IOSBlack,
                surfaceVariant = IOSLightGray,
                onSurfaceVariant = IOSGrayText,
                error = ErrorRed,
                onError = White,
                outline = IOSLightGray,
                outlineVariant = IOSGrayText
            )
            AppThemeColor.Purple -> lightColorScheme(
                primary = ThemePurple,
                onPrimary = White,
                primaryContainer = ThemePurpleLight,
                onPrimaryContainer = White,
                secondary = ThemePurple,
                onSecondary = White,
                secondaryContainer = ThemePurpleLight,
                onSecondaryContainer = ThemePurpleDark,
                tertiary = ThemePurple,
                onTertiary = White,
                background = IOSBackground,
                onBackground = IOSBlack,
                surface = IOSSurface,
                onSurface = IOSBlack,
                surfaceVariant = IOSLightGray,
                onSurfaceVariant = IOSGrayText,
                error = ErrorRed,
                onError = White,
                outline = IOSLightGray,
                outlineVariant = IOSGrayText
            )
            AppThemeColor.Orange -> lightColorScheme(
                primary = ThemeOrange,
                onPrimary = White,
                primaryContainer = ThemeOrange,
                onPrimaryContainer = White,
                secondary = ThemeOrange,
                onSecondary = White,
                secondaryContainer = ThemeOrange,
                onSecondaryContainer = ThemeOrange,
                tertiary = ThemeOrange,
                onTertiary = White,
                background = IOSBackground,
                onBackground = IOSBlack,
                surface = IOSSurface,
                onSurface = IOSBlack,
                surfaceVariant = IOSLightGray,
                onSurfaceVariant = IOSGrayText,
                error = ErrorRed,
                onError = White,
                outline = IOSLightGray,
                outlineVariant = IOSGrayText
            )
            AppThemeColor.Pink -> lightColorScheme(
                primary = ThemePink,
                onPrimary = White,
                primaryContainer = ThemePink,
                onPrimaryContainer = White,
                secondary = ThemePink,
                onSecondary = White,
                secondaryContainer = ThemePink,
                onSecondaryContainer = ThemePink,
                tertiary = ThemePink,
                onTertiary = White,
                background = IOSBackground,
                onBackground = IOSBlack,
                surface = IOSSurface,
                onSurface = IOSBlack,
                surfaceVariant = IOSLightGray,
                onSurfaceVariant = IOSGrayText,
                error = ErrorRed,
                onError = White,
                outline = IOSLightGray,
                outlineVariant = IOSGrayText
            )
        }
    }
    
    // 应用主题
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 设置状态栏颜色为 Surface 颜色 (白色)，与 TopBar 保持一致
            window.statusBarColor = colorScheme.surface.toArgb()
            // 设置状态栏图标颜色：亮色主题下使用深色图标，暗色主题下使用亮色图标
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}