package com.example.sharealarm.data.local.converter

import androidx.room.TypeConverter

/**
 * 字符串列表转换器
 * 功能：Room数据库类型转换器，将String列表转换为String类型存储，反之亦然
 */
class StringListConverter {
    /**
     * 将String列表转换为String
     * @param strings 字符串列表
     * @return 逗号分隔的字符串
     */
    @TypeConverter
    fun fromStringList(strings: List<String>?): String? {
        return strings?.joinToString(",")
    }
    
    /**
     * 将String转换为String列表
     * @param string 逗号分隔的字符串
     * @return 字符串列表
     */
    @TypeConverter
    fun toStringList(string: String?): List<String>? {
        return string?.split(",")
    }
}