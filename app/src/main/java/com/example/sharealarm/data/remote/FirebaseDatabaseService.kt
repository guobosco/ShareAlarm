package com.example.sharealarm.data.remote

import com.example.sharealarm.data.model.Organization
import com.example.sharealarm.data.model.Reminder
import com.example.sharealarm.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firebase 数据库服务
 * 功能：封装 Firebase Firestore 数据库相关的操作，包括用户、组织和提醒的增删改查
 * @property firestore Firebase Firestore 实例
 */
class FirebaseDatabaseService(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    /**
     * 保存用户信息到数据库
     * @param user 用户对象
     */
    suspend fun saveUser(user: User) {
        firestore.collection("users").document(user.id).set(user).await()
    }

    /**
     * 从数据库获取用户信息
     * @param userId 用户 ID
     * @return 用户对象，如果不存在则返回 null
     */
    suspend fun getUser(userId: String): User? {
        val document = firestore.collection("users").document(userId).get().await()
        return if (document.exists()) {
            document.toObject(User::class.java)
        } else {
            null
        }
    }

    /**
     * 创建组织
     * @param organization 组织对象
     * @return 创建成功后的组织对象
     */
    suspend fun createOrganization(organization: Organization): Organization {
        val documentRef = firestore.collection("organizations").document()
        val newOrganization = organization.copy(id = documentRef.id)
        documentRef.set(newOrganization).await()
        return newOrganization
    }

    /**
     * 获取用户所在的组织列表
     * @param userId 用户 ID
     * @return 组织列表
     */
    suspend fun getOrganizations(userId: String): List<Organization> {
        val snapshot = firestore.collection("organizations").whereArrayContains("members", userId).get().await()
        return snapshot.toObjects(Organization::class.java)
    }

    /**
     * 获取组织内的所有成员
     * @param organizationId 组织 ID
     * @return 成员列表
     */
    suspend fun getOrganizationMembers(organizationId: String): List<User> {
        val organization = firestore.collection("organizations").document(organizationId).get().await()
        val memberIds = organization.get("members") as? List<String> ?: emptyList()
        
        val members = mutableListOf<User>()
        for (memberId in memberIds) {
            val user = getUser(memberId)
            if (user != null) {
                members.add(user)
            }
        }
        return members
    }

    /**
     * 创建提醒
     * @param reminder 提醒对象
     * @return 创建成功后的提醒对象
     */
    suspend fun createReminder(reminder: Reminder): Reminder {
        val documentRef = firestore.collection("reminders").document()
        val newReminder = reminder.copy(id = documentRef.id)
        documentRef.set(newReminder).await()
        return newReminder
    }

    /**
     * 获取组织内的所有提醒
     * @param organizationId 组织 ID
     * @return 提醒列表
     */
    suspend fun getReminders(organizationId: String): List<Reminder> {
        val snapshot = firestore.collection("reminders").whereEqualTo("organizationId", organizationId).get().await()
        return snapshot.toObjects(Reminder::class.java)
    }

    /**
     * 获取用户的所有提醒
     * @param userId 用户 ID
     * @return 提醒列表
     */
    suspend fun getUserReminders(userId: String): List<Reminder> {
        val snapshot = firestore.collection("reminders").whereArrayContains("reminderUsers", userId).get().await()
        return snapshot.toObjects(Reminder::class.java)
    }
}
