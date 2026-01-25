package com.example.sharealarm.data.repository

import com.example.sharealarm.data.model.User
import com.example.sharealarm.data.remote.CloudBaseAuthService
import com.example.sharealarm.data.remote.CloudBaseDatabaseService

/**
 * 认证仓库
 * 功能：封装认证相关的业务逻辑，协调CloudBaseAuthService和CloudBaseDatabaseService
 * @property authService CloudBase认证服务
 * @property databaseService CloudBase数据库服务
 */
class AuthRepository(private val authService: CloudBaseAuthService, private val databaseService: CloudBaseDatabaseService) {
    
    /**
     * 用户注册
     * @param email 用户邮箱
     * @param password 用户密码
     * @param name 用户姓名
     * @return 注册结果，成功返回User对象，失败返回异常
     */
    suspend fun signUp(email: String, password: String, name: String) = authService.signUp(email, password, name)
    
    /**
     * 用户登录
     * @param email 用户邮箱
     * @param password 用户密码
     * @return 登录结果，成功返回User对象，失败返回异常
     */
    suspend fun signIn(email: String, password: String) = authService.signIn(email, password)
    
    /**
     * 重置密码
     * @param email 用户邮箱
     * @return 重置密码结果
     */
    suspend fun resetPassword(email: String) = authService.resetPassword(email)
    
    /**
     * 用户登出
     */
    fun signOut() = authService.signOut()
    
    /**
     * 获取当前登录用户
     * @return 当前登录用户，未登录返回null
     */
    fun getCurrentUser() = authService.currentUser
    
    /**
     * 保存用户信息到CloudBase数据库
     * @param user 用户对象
     * @return 保存结果
     */
    suspend fun saveUserToDatabase(user: User) = databaseService.saveUser(user)
    
    /**
     * 从CloudBase数据库获取用户信息
     * @param userId 用户ID
     * @return 用户信息，不存在返回null
     */
    suspend fun getUserFromDatabase(userId: String) = databaseService.getUser(userId)
}