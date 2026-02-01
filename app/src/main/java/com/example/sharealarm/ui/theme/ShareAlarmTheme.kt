package com.example.sharealarm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    // 选择颜色方案
    val colorScheme = when {
        darkTheme -> BuddyBellDarkScheme
        else -> BuddyBellLightScheme
    }
    
    // 应用主题
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}