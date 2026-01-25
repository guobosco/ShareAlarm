package com.tencent.tcb.cloudbase

class CloudBaseAuth {
    private var currentUserInfo: Map<String, Any>? = null

    fun getUserInfo(): Map<String, Any>? {
        return currentUserInfo
    }

    fun signUpWithEmail(email: String, password: String): Map<String, Any> {
        // Mock sign up
        println("Mock sign up with email: $email")
        val userId = "user_${System.currentTimeMillis()}"
        return mapOf(
            "_id" to userId,
            "email" to email
        )
    }

    fun signInWithEmail(email: String, password: String): Map<String, Any> {
        // Mock sign in
        println("Mock sign in with email: $email")
        val userId = "user_${System.currentTimeMillis()}"
        currentUserInfo = mapOf(
            "_id" to userId,
            "email" to email,
            "nickName" to email.split('@')[0]
        )
        return currentUserInfo!!
    }

    fun updateUserInfo(userInfo: Map<String, Any>) {
        // Mock update user info
        println("Mock update user info: $userInfo")
        currentUserInfo = (currentUserInfo ?: emptyMap()) + userInfo
    }

    fun sendResetPasswordEmail(email: String) {
        // Mock send reset password email
        println("Mock send reset password email to: $email")
    }

    fun signOut() {
        // Mock sign out
        println("Mock sign out")
        currentUserInfo = null
    }
}
