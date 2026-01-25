package com.example.sharealarm.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 颜色常量定义
val Blue5 = Color(0xFFE6F0FF)
val Blue10 = Color(0xFFCCE0FF)
val Blue20 = Color(0xFF99C2FF)
val Blue40 = Color(0xFF3399FF)
val Blue80 = Color(0xFF0066CC)
val Blue90 = Color(0xFF0052A3)
val Blue95 = Color(0xFFE6F0FF)

val Orange5 = Color(0xFFFFF0E6)
val Orange10 = Color(0xFFFFE0CC)
val Orange20 = Color(0xFFFFC299)
val Orange40 = Color(0xFFFF9933)
val Orange80 = Color(0xFFCC6600)
val Orange90 = Color(0xFFA35200)
val Orange95 = Color(0xFFFFF0E6)

val Green5 = Color(0xFFE6FFE6)
val Green10 = Color(0xFFCCFFCC)
val Green20 = Color(0xFF99FF99)
val Green40 = Color(0xFF33CC33)
val Green80 = Color(0xFF009900)
val Green90 = Color(0xFF007A00)
val Green95 = Color(0xFFE6FFE6)

val Red5 = Color(0xFFFFE6E6)
val Red10 = Color(0xFFFFCCCC)
val Red20 = Color(0xFFFF9999)
val Red40 = Color(0xFFFF3333)
val Red80 = Color(0xFFCC0000)
val Red90 = Color(0xFFA30000)
val Red95 = Color(0xFFFFE6E6)

val Grey5 = Color(0xFFF5F5F5)
val Grey10 = Color(0xFFE0E0E0)
val Grey20 = Color(0xFFCCCCCC)
val Grey80 = Color(0xFF4D4D4D)
val Grey90 = Color(0xFF333333)

val BlueGrey5 = Color(0xFFF0F0F5)
val BlueGrey10 = Color(0xFFE0E0EB)
val BlueGrey20 = Color(0xFFC0C0D0)
val BlueGrey30 = Color(0xFF9999B2)
val BlueGrey50 = Color(0xFF666680)
val BlueGrey60 = Color(0xFF4D4D66)
val BlueGrey80 = Color(0xFF33334D)
val BlueGrey95 = Color(0xFFF0F0F5)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

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
