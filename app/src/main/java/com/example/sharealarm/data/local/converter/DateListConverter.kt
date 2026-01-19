package com.example.sharealarm.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * 日期列表转换器
 * 功能：Room数据库类型转换器，将Date列表转换为String类型存储，反之亦然
 */
class DateListConverter {
    /**
     * 将Date列表转换为String
     * @param dates 日期列表
     * @return 逗号分隔的时间戳字符串
     */
    @TypeConverter
    fun fromDateList(dates: List<Date>?): String? {
        return dates?.joinToString(",") { it.time.toString() }
    }
    
    /**
     * 将String转换为Date列表
     * @param dateString 逗号分隔的时间戳字符串
     * @return 日期列表
     */
    @TypeConverter
    fun toDateList(dateString: String?): List<Date>? {
        return dateString?.split(",")?.mapNotNull { 
            try {
                Date(it.toLong())
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
}