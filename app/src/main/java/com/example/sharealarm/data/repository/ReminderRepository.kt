package com.example.sharealarm.data.repository

import com.example.sharealarm.data.model.Reminder
import com.example.sharealarm.data.remote.CloudbaseDatabaseService

/**
 * 提醒仓库
 * 功能：封装提醒相关的业务逻辑，协调CloudbaseDatabaseService
 * @property databaseService Cloudbase数据库服务
 */
class ReminderRepository(private val databaseService: CloudbaseDatabaseService) {
    
    /**
     * 创建提醒
     * @param reminder 提醒对象
     * @return 创建结果，成功返回Reminder对象，失败返回异常
     */
    suspend fun createReminder(reminder: Reminder) = databaseService.createReminderResult(reminder)
    
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
    suspend fun updateReminder(reminder: Reminder) = databaseService.updateReminderResult(reminder)
    
    /**
     * 删除提醒
     * @param reminderId 提醒ID
     * @return 删除结果
     */
    suspend fun deleteReminder(reminderId: String) = databaseService.deleteReminderResult(reminderId)
}