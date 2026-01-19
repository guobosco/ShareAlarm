package com.example.sharealarm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sharealarm.data.local.entity.LocalReminder

/**
 * 提醒数据访问对象
 * 功能：定义对LocalReminder实体的数据库操作方法
 */
@Dao
interface ReminderDao {
    /**
     * 获取所有提醒
     * @return 提醒列表
     */
    @Query("SELECT * FROM local_reminders")
    suspend fun getAllReminders(): List<LocalReminder>
    
    /**
     * 根据ID获取提醒
     * @param id 提醒ID
     * @return 提醒对象，不存在返回null
     */
    @Query("SELECT * FROM local_reminders WHERE id = :id")
    suspend fun getReminderById(id: String): LocalReminder?
    
    /**
     * 插入提醒
     * @param reminder 提醒对象
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: LocalReminder)
    
    /**
     * 更新提醒
     * @param reminder 提醒对象
     */
    @Update
    suspend fun updateReminder(reminder: LocalReminder)
    
    /**
     * 删除提醒
     * @param reminder 提醒对象
     */
    @Delete
    suspend fun deleteReminder(reminder: LocalReminder)
    
    /**
     * 根据ID删除提醒
     * @param id 提醒ID
     */
    @Query("DELETE FROM local_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: String)
    
    /**
     * 删除所有提醒
     */
    @Query("DELETE FROM local_reminders")
    suspend fun deleteAllReminders()
}