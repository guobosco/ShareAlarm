package com.example.sharealarm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sharealarm.data.local.converter.DateConverter
import com.example.sharealarm.data.local.converter.DateListConverter
import com.example.sharealarm.data.local.converter.StringListConverter
import com.example.sharealarm.data.local.dao.ReminderDao
import com.example.sharealarm.data.local.entity.LocalReminder

/**
 * 闹钟本地数据库
 * 功能：定义本地数据库的结构，包括实体类和DAO接口
 * @property reminderDao 提醒数据访问对象
 */
@Database(
    entities = [LocalReminder::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, DateListConverter::class, StringListConverter::class)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}