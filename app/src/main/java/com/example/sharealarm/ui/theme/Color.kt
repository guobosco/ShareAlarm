package com.example.sharealarm.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 暖橙/铃铛黄 主题色
val BellYellow = Color(0xFFFFB340) // 核心主色
val BellYellowLight = Color(0xFFFFCC00) // 亮色
val BellYellowDark = Color(0xFFE69500) // 深色
val BellOrange = Color(0xFFFF9500) // 辅助橙色

// 蓝色主题 (默认主色 #3970FF)
val ThemeBlue = Color(0xFF3970FF)
val ThemeBlueLight = Color(0xFF4E51BF)
val ThemeBlueDark = Color(0xFF6352CA)

// 绿色主题 (新 #00966B)
val ThemeGreen = Color(0xFF00966B)
val ThemeGreenLight = Color(0xFF00C853)
val ThemeGreenDark = Color(0xFF00695C)

// 紫色主题
val ThemePurple = Color(0xFF6A1B9A)
val ThemePurpleLight = Color(0xFF8E24AA)
val ThemePurpleDark = Color(0xFF4A148C)

// 新增颜色
val ThemeOrange = Color(0xFFFF7530)
val ThemePink = Color(0xFFFF8CA4)

// iOS 风格灰度
val IOSBackground = Color(0xFFF2F2F7) // 背景灰
val IOSSurface = Color(0xFFFFFFFF) // 卡片白
val IOSGrayText = Color(0xFF8E8E93) // 辅助文本灰
val IOSLightGray = Color(0xFFE5E5EA) // 分割线/浅色背景
val IOSBlack = Color(0xFF1A1A1A) // 主要文本黑

// 语义色
val SuccessGreen = Color(0xFF34C759)
val ErrorRed = Color(0xFFFF3B30)
val LinkBlue = Color(0xFF007AFF)

// 旧颜色保留（按需清理）
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
// 默认使用亮色主题，符合极简日历风格
val BuddyBellLightScheme = lightColorScheme(
    primary = BellYellow,
    onPrimary = White,
    primaryContainer = BellYellowLight,
    onPrimaryContainer = Black,
    secondary = BellOrange,
    onSecondary = White,
    secondaryContainer = Orange10,
    onSecondaryContainer = Orange90,
    tertiary = LinkBlue,
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

// 暗色主题（暂不重点适配，保持基础可用性）
val BuddyBellDarkScheme = darkColorScheme(
    primary = BellYellowDark,
    onPrimary = Black,
    primaryContainer = BellYellowDark,
    onPrimaryContainer = Black,
    background = Color(0xFF000000),
    onBackground = White,
    surface = Color(0xFF1C1C1E),
    onSurface = White
)

val NASABlueColorScheme = BuddyBellDarkScheme // 暂时映射，避免报错

// 亮色主题（备用，根据需求可以启用）
val LightColorScheme = BuddyBellLightScheme
