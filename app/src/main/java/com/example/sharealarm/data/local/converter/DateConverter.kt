package com.example.sharealarm.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * 日期转换器
 * 功能：Room数据库类型转换器，将Date对象转换为Long类型存储，反之亦然
 */
class DateConverter {
    /**
     * 将Date转换为Long
     * @param date 日期对象
     * @return 时间戳
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * 将Long转换为Date
     * @param timestamp 时间戳
     * @return 日期对象
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}