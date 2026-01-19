package com.example.sharealarm.data.remote

import com.example.sharealarm.data.model.Organization
import com.example.sharealarm.data.model.Reminder
import com.example.sharealarm.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Firebase Firestore服务
 * 功能：封装Firebase Firestore相关的操作，包括用户、组织和提醒的数据管理
 */
class FirebaseFirestoreService {
    // Firebase Firestore实例
    private val db: FirebaseFirestore = Firebase.firestore
    
    // 集合名称常量
    private val USERS_COLLECTION = "users"
    private val ORGANIZATIONS_COLLECTION = "organizations"
    private val REMINDERS_COLLECTION = "reminders"
    
    /**
     * 保存用户信息
     * @param user 用户对象
     * @return 保存结果，成功返回Result.success(Unit)，失败返回Result.failure(exception)
     */
    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            db.collection(USERS_COLLECTION).document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息，成功返回Result.success(User)，失败返回Result.failure(exception)
     */
    suspend fun getUser(userId: String): Result<User> {
        return try {
            val snapshot = db.collection(USERS_COLLECTION).document(userId).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建组织
     * @param name 组织名称
     * @param creatorId 创建者ID
     * @return 创建结果，成功返回Result.success(Organization)，失败返回Result.failure(exception)
     */
    suspend fun createOrganization(name: String, creatorId: String): Result<Organization> {
        return try {
            val organization = Organization(
                name = name,
                creatorId = creatorId,
                members = listOf(creatorId)
            )
            val docRef = db.collection(ORGANIZATIONS_COLLECTION).add(organization).await()
            val createdOrganization = organization.copy(id = docRef.id)
            db.collection(ORGANIZATIONS_COLLECTION).document(docRef.id).set(createdOrganization).await()
            Result.success(createdOrganization)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 加入组织
     * @param userId 用户ID
     * @param orgId 组织ID
     * @return 加入结果，成功返回Result.success(Unit)，失败返回Result.failure(exception)
     */
    suspend fun joinOrganization(userId: String, orgId: String): Result<Unit> {
        return try {
            val orgDoc = db.collection(ORGANIZATIONS_COLLECTION).document(orgId).get().await()
            val organization = orgDoc.toObject(Organization::class.java)
            if (organization != null) {
                val updatedMembers = organization.members.toMutableList()
                if (!updatedMembers.contains(userId)) {
                    updatedMembers.add(userId)
                    db.collection(ORGANIZATIONS_COLLECTION).document(orgId)
                        .update("members", updatedMembers)
                        .await()
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Organization not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 退出组织
     * @param userId 用户ID
     * @param orgId 组织ID
     * @return 退出结果，成功返回Result.success(Unit)，失败返回Result.failure(exception)
     */
    suspend fun leaveOrganization(userId: String, orgId: String): Result<Unit> {
        return try {
            val orgDoc = db.collection(ORGANIZATIONS_COLLECTION).document(orgId).get().await()
            val organization = orgDoc.toObject(Organization::class.java)
            if (organization != null) {
                val updatedMembers = organization.members.toMutableList()
                updatedMembers.remove(userId)
                db.collection(ORGANIZATIONS_COLLECTION).document(orgId)
                    .update("members", updatedMembers)
                    .await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Organization not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取用户所属的组织列表
     * @param userId 用户ID
     * @return 组织列表，成功返回Result.success(List<Organization>)，失败返回Result.failure(exception)
     */
    suspend fun getOrganizationsByUser(userId: String): Result<List<Organization>> {
        return try {
            val querySnapshot = db.collection(ORGANIZATIONS_COLLECTION)
                .whereArrayContains("members", userId)
                .get()
                .await()
            val organizations = querySnapshot.documents.mapNotNull { it.toObject(Organization::class.java) }
            Result.success(organizations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取组织详情
     * @param orgId 组织ID
     * @return 组织对象，成功返回Result.success(Organization)，失败返回Result.failure(exception)
     */
    suspend fun getOrganizationById(orgId: String): Result<Organization> {
        return try {
            val snapshot = db.collection(ORGANIZATIONS_COLLECTION).document(orgId).get().await()
            val organization = snapshot.toObject(Organization::class.java)
            if (organization != null) {
                Result.success(organization)
            } else {
                Result.failure(Exception("Organization not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建提醒
     * @param reminder 提醒对象
     * @return 创建结果，成功返回Result.success(Reminder)，失败返回Result.failure(exception)
     */
    suspend fun createReminder(reminder: Reminder): Result<Reminder> {
        return try {
            val docRef = db.collection(REMINDERS_COLLECTION).add(reminder).await()
            val createdReminder = reminder.copy(id = docRef.id)
            db.collection(REMINDERS_COLLECTION).document(docRef.id).set(createdReminder).await()
            Result.success(createdReminder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新提醒
     * @param reminder 提醒对象
     * @return 更新结果，成功返回Result.success(Unit)，失败返回Result.failure(exception)
     */
    suspend fun updateReminder(reminder: Reminder): Result<Unit> {
        return try {
            db.collection(REMINDERS_COLLECTION).document(reminder.id).set(reminder).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除提醒
     * @param reminderId 提醒ID
     * @return 删除结果，成功返回Result.success(Unit)，失败返回Result.failure(exception)
     */
    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            db.collection(REMINDERS_COLLECTION).document(reminderId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取组织的提醒列表
     * @param orgId 组织ID
     * @return 提醒列表，成功返回Result.success(List<Reminder>)，失败返回Result.failure(exception)
     */
    suspend fun getRemindersByOrganization(orgId: String): Result<List<Reminder>> {
        return try {
            val querySnapshot = db.collection(REMINDERS_COLLECTION)
                .whereEqualTo("orgId", orgId)
                .get()
                .await()
            val reminders = querySnapshot.documents.mapNotNull { it.toObject(Reminder::class.java) }
            Result.success(reminders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取用户的提醒列表
     * @param userId 用户ID
     * @return 提醒列表，成功返回Result.success(List<Reminder>)，失败返回Result.failure(exception)
     */
    suspend fun getRemindersByUser(userId: String): Result<List<Reminder>> {
        return try {
            val querySnapshot = db.collection(REMINDERS_COLLECTION)
                .whereArrayContains("participants", userId)
                .get()
                .await()
            val reminders = querySnapshot.documents.mapNotNull { it.toObject(Reminder::class.java) }
            Result.success(reminders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
