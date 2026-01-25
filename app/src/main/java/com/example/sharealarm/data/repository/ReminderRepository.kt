package com.example.sharealarm.data.repository

import android.content.Context
import android.util.Log
import com.example.sharealarm.data.local.AlarmDatabaseInstance
import com.example.sharealarm.data.local.entity.LocalReminder
import com.example.sharealarm.data.model.Reminder
import com.example.sharealarm.data.remote.CloudBaseDatabaseService
import com.example.sharealarm.service.AlarmScheduler

/**
 * 提醒仓库
 * 功能：封装提醒相关的业务逻辑，协调本地数据库和云服务
 * @property databaseService CloudBase数据库服务
 * @property context 应用上下文
 */
class ReminderRepository(
    private val databaseService: CloudBaseDatabaseService,
    private val context: Context
) {
    
    /**
     * 创建提醒
     * @param reminder 提醒对象
     * @return 创建结果，成功返回Reminder对象，失败返回异常
     */
    suspend fun createReminder(reminder: Reminder): Result<Reminder> {
        return try {
            // 保存到云端
            val cloudResult = databaseService.createReminderResult(reminder)
            
            if (cloudResult.isSuccess) {
                var createdReminder = cloudResult.getOrThrow()
                
                // 确保createdReminder有正确的id字段
                if (createdReminder.id.isNullOrEmpty()) {
                    // 如果id为空，使用提醒的标题和时间生成一个唯一id
                    val generatedId = "${reminder.title}_${System.currentTimeMillis()}"
                    createdReminder = createdReminder.copy(id = generatedId)
                    Log.d("ReminderRepository", "Generated reminder id: $generatedId")
                }
                
                Log.d("ReminderRepository", "Created reminder with id: ${createdReminder.id}")
                
                // 保存到本地数据库
                saveToLocalDatabase(createdReminder)
                
                // 设置系统闹钟
                if (createdReminder.alertTimes.isNotEmpty()) {
                    Log.d("ReminderRepository", "Scheduling alarms for ${createdReminder.alertTimes.size} times")
                    val successCount = AlarmScheduler.scheduleMultipleAlarms(context, createdReminder.id, createdReminder.alertTimes)
                    Log.d("ReminderRepository", "Successfully scheduled $successCount alarms")
                }
                
                Result.success(createdReminder)
            } else {
                cloudResult
            }
        } catch (e: Exception) {
            Log.e("ReminderRepository", "Failed to create reminder: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 获取组织的提醒列表
     * @param orgId 组织ID
     * @return 提醒列表
     */
    suspend fun getRemindersByOrganization(orgId: String) = databaseService.getRemindersByOrganizationResult(orgId)
    
    /**
     * 获取用户的提醒列表
     * @param userId 用户ID
     * @return 提醒列表
     */
    suspend fun getRemindersByUser(userId: String) = databaseService.getRemindersByUserResult(userId)
    
    /**
     * 更新提醒
     * @param reminder 提醒对象
     * @return 更新结果
     */
    suspend fun updateReminder(reminder: Reminder): Result<Unit> {
        return try {
            // 保存到云端
            val cloudResult = databaseService.updateReminderResult(reminder)
            
            if (cloudResult.isSuccess) {
                // 更新本地数据库
                saveToLocalDatabase(reminder)
                
                // 取消旧的闹钟
                AlarmScheduler.cancelMultipleAlarms(context, reminder.id, 10) // 假设最多10个提醒时间
                
                // 设置新的闹钟
                if (reminder.alertTimes.isNotEmpty()) {
                    AlarmScheduler.scheduleMultipleAlarms(context, reminder.id, reminder.alertTimes)
                }
            }
            
            cloudResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除提醒
     * @param reminderId 提醒ID
     * @return 删除结果
     */
    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            // 从云端删除
            val cloudResult = databaseService.deleteReminderResult(reminderId)
            
            if (cloudResult.isSuccess) {
                // 从本地数据库删除
                deleteFromLocalDatabase(reminderId)
                
                // 取消系统闹钟
                AlarmScheduler.cancelMultipleAlarms(context, reminderId, 10) // 假设最多10个提醒时间
            }
            
            cloudResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 保存提醒到本地数据库
     * @param reminder 提醒对象
     */
    private suspend fun saveToLocalDatabase(reminder: Reminder) {
        try {
            val localReminder = LocalReminder(
                id = reminder.id,
                orgId = reminder.orgId,
                title = reminder.title,
                description = reminder.description,
                eventTime = reminder.eventTime,
                location = reminder.location,
                alertTimes = reminder.alertTimes,
                participants = reminder.participants,
                creator = reminder.creator,
                createdAt = reminder.createdAt,
                updatedAt = reminder.updatedAt
            )
            
            val db = AlarmDatabaseInstance.getDatabase(context)
            db.reminderDao().insertReminder(localReminder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 从本地数据库删除提醒
     * @param reminderId 提醒ID
     */
    private suspend fun deleteFromLocalDatabase(reminderId: String) {
        try {
            val db = AlarmDatabaseInstance.getDatabase(context)
            db.reminderDao().deleteReminderById(reminderId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}