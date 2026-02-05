package com.example.sharealarm.util

import java.util.regex.Pattern

object ValidationUtils {
    /**
     * 校验电话号码格式
     * 支持常见的中国大陆手机号格式 (13, 14, 15, 16, 17, 18, 19 开头)
     */
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false
        // 简单的正则校验：1开头，第二位3-9，后面9位数字
        val regex = "^1[3-9]\\d{9}$"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(phoneNumber)
        return matcher.matches()
    }
}
