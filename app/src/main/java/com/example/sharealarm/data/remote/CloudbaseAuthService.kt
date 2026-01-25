package com.example.sharealarm.data.remote

import android.util.Log
import com.example.sharealarm.data.model.User
import com.tencent.tcb.cloudbase.CloudBaseCore
import com.tencent.tcb.cloudbase.exception.CloudBaseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * CloudBase 认证服务
 * 功能：处理用户认证相关操作，包括注册、登录、登出等
 */
class CloudBaseAuthService {
    
    // CloudBase 核心实例
    private val cloudBaseCore by lazy {
        CloudBaseCore.getInstance()
    }
    
    // 日志标签
    private val TAG = "CloudBaseAuthService"
    
    /**
     * 当前登录用户
     */
    val currentUser: User?
        get() {
            try {
                val auth = cloudBaseCore.auth()
                val userInfo = auth.getUserInfo()
                return if (userInfo != null) {
                    User(
                        id = userInfo["_id"] as? String ?: "",
                        name = userInfo["nickName"] as? String ?: userInfo["username"] as? String ?: "",
                        email = userInfo["email"] as? String ?: ""
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取当前用户失败: ${e.message}")
                return null
            }
        }
    
    /**
     * 用户注册
     * @param email 用户邮箱
     * @param password 用户密码
     * @param name 用户姓名
     * @return 注册成功后的用户对象
     */
    suspend fun signUp(email: String, password: String, name: String): User {
        return withContext(Dispatchers.IO) {
            try {
                val auth = cloudBaseCore.auth()
                
                // 注册用户
                val result = auth.signUpWithEmail(email, password)
                
                // 登录用户
                auth.signInWithEmail(email, password)
                
                // 更新用户信息
                val userInfo = mapOf(
                    "nickName" to name,
                    "email" to email
                )
                auth.updateUserInfo(userInfo)
                
                Log.d(TAG, "用户注册成功")
                User(
                    id = result["_id"] as? String ?: "",
                    name = name,
                    email = email
                )
            } catch (e: CloudBaseException) {
                Log.e(TAG, "用户注册失败: ${e.message}")
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "用户注册失败: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * 用户登录
     * @param email 用户邮箱
     * @param password 用户密码
     * @return 登录成功后的用户对象
     */
    suspend fun signIn(email: String, password: String): User {
        return withContext(Dispatchers.IO) {
            try {
                val auth = cloudBaseCore.auth()
                
                // 登录用户
                val result = auth.signInWithEmail(email, password)
                
                Log.d(TAG, "用户登录成功")
                User(
                    id = result["_id"] as? String ?: "",
                    name = result["nickName"] as? String ?: result["username"] as? String ?: "",
                    email = email
                )
            } catch (e: CloudBaseException) {
                Log.e(TAG, "用户登录失败: ${e.message}")
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "用户登录失败: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * 重置密码
     * @param email 用户邮箱
     */
    suspend fun resetPassword(email: String) {
        withContext(Dispatchers.IO) {
            try {
                val auth = cloudBaseCore.auth()
                auth.sendResetPasswordEmail(email)
                Log.d(TAG, "密码重置邮件发送成功: $email")
            } catch (e: CloudBaseException) {
                Log.e(TAG, "密码重置邮件发送失败: ${e.message}")
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "密码重置邮件发送失败: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * 手机号登录
     * @param phoneNumber 手机号
     * @param verificationCode 验证码
     * @return 登录成功后的用户对象
     */
    suspend fun signInWithPhone(phoneNumber: String, verificationCode: String): User {
        return withContext(Dispatchers.IO) {
            try {
                // 模拟手机号登录
                Log.d(TAG, "手机号登录成功: $phoneNumber")
                User(
                    id = "phone_${System.currentTimeMillis()}",
                    name = "用户_${phoneNumber.takeLast(4)}",
                    email = ""
                )
            } catch (e: Exception) {
                Log.e(TAG, "手机号登录失败: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * 发送手机号验证码
     * @param phoneNumber 手机号
     */
    suspend fun sendPhoneVerificationCode(phoneNumber: String) {
        withContext(Dispatchers.IO) {
            try {
                // 模拟发送验证码，默认验证码为 123456
                val defaultCode = "123456"
                Log.d(TAG, "手机号验证码发送成功: $phoneNumber, 验证码: $defaultCode")
            } catch (e: Exception) {
                Log.e(TAG, "手机号验证码发送失败: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * 微信授权登录
     * @return 登录成功后的用户对象
     */
    suspend fun signInWithWechat(): User {
        return withContext(Dispatchers.IO) {
            try {
                // 模拟微信授权登录
                Log.d(TAG, "微信授权登录成功")
                User(
                    id = "wechat_${System.currentTimeMillis()}",
                    name = "微信用户",
                    email = ""
                )
            } catch (e: Exception) {
                Log.e(TAG, "微信授权登录失败: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * 用户登出
     */
    fun signOut() {
        try {
            val auth = cloudBaseCore.auth()
            auth.signOut()
            Log.d(TAG, "用户登出成功")
        } catch (e: Exception) {
            Log.e(TAG, "用户登出失败: ${e.message}")
        }
    }
}
