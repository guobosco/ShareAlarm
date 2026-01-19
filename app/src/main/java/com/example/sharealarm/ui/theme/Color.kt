package com.example.sharealarm.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/**
 * 应用颜色主题
 * 功能：定义应用的颜色方案，包括亮色和暗色主题
 */
val NASABlueColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Orange80,
    onSecondary = Orange20,
    secondaryContainer = Orange90,
    onSecondaryContainer = Orange10,
    tertiary = Green80,
    onTertiary = Green20,
    tertiaryContainer = Green90,
    onTertiaryContainer = Green10,
    error = Red80,
    onError = Red20,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Grey90,
    onBackground = Grey10,
    surface = Grey80,
    onSurface = Grey20,
    surfaceVariant = BlueGrey80,
    onSurfaceVariant = BlueGrey20,
    outline = BlueGrey60,
    outlineVariant = BlueGrey30,
    scrim = Black,
    inverseSurface = Grey20,
    inverseOnSurface = Grey90,
    inversePrimary = Blue40,
)

// 亮色主题（备用，根据需求可以启用）
val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = White,
    primaryContainer = Blue95,
    onPrimaryContainer = Blue5,
    secondary = Orange40,
    onSecondary = White,
    secondaryContainer = Orange95,
    onSecondaryContainer = Orange5,
    tertiary = Green40,
    onTertiary = White,
    tertiaryContainer = Green95,
    onTertiaryContainer = Green5,
    error = Red40,
    onError = White,
    errorContainer = Red95,
    onErrorContainer = Red5,
    background = Grey5,
    onBackground = Grey90,
    surface = White,
    onSurface = Grey90,
    surfaceVariant = BlueGrey95,
    onSurfaceVariant = BlueGrey30,
    outline = BlueGrey50,
    outlineVariant = BlueGrey80,
    scrim = Black,
    inverseSurface = Grey90,
    inverseOnSurface = Grey5,
    inversePrimary = Blue80,
)
