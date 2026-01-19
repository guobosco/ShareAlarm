package com.example.sharealarm.data.remote

import android.content.Context
import com.example.sharealarm.data.model.User
import com.tencent.cloudbase.auth.Auth
import com.tencent.cloudbase.auth.AuthException
import com.tencent.cloudbase.auth.LoginState
import com.tencent.cloudbase.database.CloudbaseDatabase
import com.tencent.cloudbase.database.Query
import com.tencent.cloudbase.database.Where
import com.tencent.cloudbase.database.enums.QueryCommand
import com.tencent.cloudbase.exception.CloudbaseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cloudbase 认证服务
 * 功能：封装 Cloudbase 认证相关的操作，包括注册、登录、登出和重置密码
 * @property auth Cloudbase 认证实例
 * @property db Cloudbase 数据库实例
 */
class CloudbaseAuthService(private val auth: Auth, private val db: CloudbaseDatabase) {

    /**
     * 当前登录用户
     */
    val currentUser: User?
        get() {
            val loginState = auth.getLoginState()
            return if (loginState != null && loginState.isLogin) {
                User(
                    id = loginState.userId,
                    name = loginState.nickName ?: "",
                    email = loginState.email ?: ""
                )
            } else {
                null
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
                // 使用邮箱密码注册
                val loginState = auth.signUpWithEmail(email, password)
                
                // 保存用户信息到数据库
                val usersCollection = db.collection("users")
                val userData = mapOf(
                    "id" to loginState.userId,
                    "name" to name,
                    "email" to email
                )
                usersCollection.add(userData)
                
                return@withContext User(
                    id = loginState.userId,
                    name = name,
                    email = email
                )
            } catch (e: Exception) {
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
                // 使用邮箱密码登录
                val loginState = auth.signInWithEmail(email, password)
                
                // 从数据库获取用户信息
                val usersCollection = db.collection("users")
                val query = usersCollection.where(
                    Where(
                        "id",
                        QueryCommand.EQ,
                        loginState.userId
                    )
                )
                val result = query.get()
                val userData = result.data.firstOrNull()?.data
                
                val userName = userData?.get("name") as? String ?: ""
                
                return@withContext User(
                    id = loginState.userId,
                    name = userName,
                    email = email
                )
            } catch (e: Exception) {
                throw e
            }
        }
    }

    /**
     * 重置密码
     * @param email 用户邮箱
     */
    suspend fun resetPassword(email: String) {
        return withContext(Dispatchers.IO) {
            try {
                // 发送密码重置邮件
                auth.sendPasswordResetEmail(email)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    /**
     * 用户登出
     */
    fun signOut() {
        auth.signOut()
    }
}