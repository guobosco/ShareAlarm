package com.example.sharealarm.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeColor {
    Yellow, Blue, Green, Purple, Orange, Pink
}

object ThemeSettings {
    // 默认使用浅色模式 (false)
    private val _isDarkTheme = MutableStateFlow<Boolean?>(false)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    // 当前主题色，默认为蓝色
    private val _themeColor = MutableStateFlow(AppThemeColor.Blue)
    val themeColor = _themeColor.asStateFlow()

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }
    
    fun setFollowSystem() {
        _isDarkTheme.value = null
    }

    fun setThemeColor(color: AppThemeColor) {
        _themeColor.value = color
    }
}
