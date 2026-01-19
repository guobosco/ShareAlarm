package com.example.sharealarm.data.local

import android.content.Context
import androidx.room.Room

/**
 * 数据库实例管理类
 * 功能：提供单例模式的数据库实例访问
 */
object AlarmDatabaseInstance {
    // 数据库实例，使用volatile关键字确保多线程可见性
    @Volatile
    private var INSTANCE: AlarmDatabase? = null
    
    /**
     * 获取数据库实例
     * @param context 上下文
     * @return 数据库实例
     */
    fun getDatabase(context: Context): AlarmDatabase {
        // 双重检查锁定模式，确保线程安全和高效性
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AlarmDatabase::class.java,
                "alarm_database"
            )
                .fallbackToDestructiveMigration() // 数据库版本升级时，如果没有迁移策略则销毁重建
                .build()
            INSTANCE = instance
            instance
        }
    }
}