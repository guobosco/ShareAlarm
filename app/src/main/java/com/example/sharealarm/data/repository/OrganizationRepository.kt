package com.example.sharealarm.data.repository

import com.example.sharealarm.data.model.Organization
import com.example.sharealarm.data.remote.CloudBaseDatabaseService

/**
 * 组织仓库
 * 功能：封装组织相关的业务逻辑，协调CloudBaseDatabaseService
 * @property databaseService CloudBase数据库服务
 */
class OrganizationRepository(private val databaseService: CloudBaseDatabaseService) {
    
    /**
     * 创建组织
     * @param name 组织名称
     * @param creatorId 创建者ID
     * @return 创建结果，成功返回Organization对象，失败返回异常
     */
    suspend fun createOrganization(name: String, creatorId: String) = databaseService.createOrganizationResult(name, creatorId)
    
    /**
     * 加入组织
     * @param userId 用户ID
     * @param orgId 组织ID
     * @return 加入结果
     */
    suspend fun joinOrganization(userId: String, orgId: String) = databaseService.joinOrganizationResult(userId, orgId)
    
    /**
     * 获取用户所属的组织列表
     * @param userId 用户ID
     * @return 组织列表
     */
    suspend fun getOrganizationsByUser(userId: String) = databaseService.getOrganizationsByUserResult(userId)
    
    /**
     * 获取组织详情
     * @param orgId 组织ID
     * @return 组织对象，不存在返回null
     */
    suspend fun getOrganizationById(orgId: String) = databaseService.getOrganizationByIdResult(orgId)
    
    /**
     * 退出组织
     * @param userId 用户ID
     * @param orgId 组织ID
     * @return 退出结果
     */
    suspend fun leaveOrganization(userId: String, orgId: String) = databaseService.leaveOrganizationResult(userId, orgId)
}