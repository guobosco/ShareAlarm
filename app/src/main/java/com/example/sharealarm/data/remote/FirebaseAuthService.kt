package com.example.sharealarm.data.remote

import com.example.sharealarm.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firebase 认证服务
 * 功能：封装 Firebase 认证相关的操作，包括注册、登录、登出和重置密码
 * @property auth Firebase 认证实例
 * @property firestore Firebase Firestore 实例
 */
class FirebaseAuthService(private val auth: FirebaseAuth = FirebaseAuth.getInstance(), private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    /**
     * 当前登录用户
     */
    val currentUser: User?
        get() = auth.currentUser?.let { mapFirebaseUserToUser(it) }

    /**
     * 用户注册
     * @param email 用户邮箱
     * @param password 用户密码
     * @param name 用户姓名
     * @return 注册成功后的用户对象
     */
    suspend fun signUp(email: String, password: String, name: String): User {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("注册失败，用户为空")
        
        // 保存用户信息到 Firestore
        val userData = mapOf(
            "name" to name,
            "email" to email,
            "uid" to user.uid
        )
        firestore.collection("users").document(user.uid).set(userData).await()
        
        return User(user.uid, name, email)
    }

    /**
     * 用户登录
     * @param email 用户邮箱
     * @param password 用户密码
     * @return 登录成功后的用户对象
     */
    suspend fun signIn(email: String, password: String): User {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("登录失败，用户为空")
        
        // 从 Firestore 获取用户信息
        val userDoc = firestore.collection("users").document(user.uid).get().await()
        val name = userDoc.getString("name") ?: ""
        
        return User(user.uid, name, email)
    }

    /**
     * 重置密码
     * @param email 用户邮箱
     */
    suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    /**
     * 用户登出
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * 将 FirebaseUser 转换为 User 对象
     * @param firebaseUser FirebaseUser 对象
     * @return User 对象
     */
    private fun mapFirebaseUserToUser(firebaseUser: FirebaseUser): User {
        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: ""
        )
    }
}
