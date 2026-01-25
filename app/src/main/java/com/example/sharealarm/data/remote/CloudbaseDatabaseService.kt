package com.example.sharealarm.data.remote

import android.util.Log
import com.example.sharealarm.data.model.Organization
import com.example.sharealarm.data.model.Reminder
import com.example.sharealarm.data.model.User
import com.tencent.tcb.cloudbase.CloudBaseCore
import com.tencent.tcb.cloudbase.database.CloudBaseQuery
import com.tencent.tcb.cloudbase.database.CloudBaseDatabase
import com.tencent.tcb.cloudbase.database.CloudBaseResult
import com.tencent.tcb.cloudbase.exception.CloudBaseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * CloudBase 数据库服务
 * 功能：处理与 CloudBase 数据库相关的操作
 */
class CloudBaseDatabaseService {
    
    // CloudBase 核心实例
    private val cloudBaseCore = CloudBaseCore.getInstance()
    
    // CloudBase 数据库实例
    private val database = cloudBaseCore.database()
    
    // 日志标签
    private val TAG = "CloudBaseDatabaseService"
    
    /**
     * 保存用户信息到 CloudBase 数据库
     * @param user 用户对象
     */
    suspend fun saveUser(user: User) {
        withContext(Dispatchers.IO) {
            try {
                val usersCollection = database.collection("users")
                usersCollection.doc(user.id).set(user)
                Log.d(TAG, "用户信息保存成功: ${user.id}")
            } catch (e: Exception) {
                Log.e(TAG, "用户信息保存失败: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * 从 CloudBase 数据库获取用户信息
     * @param userId 用户 ID
     * @return 用户信息，不存在返回 null
     */
    suspend fun getUser(userId: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val usersCollection = database.collection("users")
                val result = usersCollection.doc(userId).get()
                val data = result.data
                if (data != null) {
                    val user = User(
                        id = data["id"] as? String ?: "",
                        name = data["name"] as? String ?: "",
                        email = data["email"] as? String ?: ""
                    )
                    Log.d(TAG, "用户信息获取成功: $userId")
                    user
                } else {
                    Log.d(TAG, "用户信息不存在: $userId")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "用户信息获取失败: ${e.message}")
                null
            }
        }
    }
    
    /**
     * 创建组织
     * @param organization 组织信息
     * @return 创建成功后的组织对象
     */
    suspend fun createOrganization(organization: Organization): Organization {
        return withContext(Dispatchers.IO) {
            try {
                val organizationsCollection = database.collection("organizations")
                val result = organizationsCollection.add(organization)
                val newOrganization = organization.copy(id = result.id)
                Log.d(TAG, "组织创建成功: ${newOrganization.id}")
                newOrganization
            } catch (e: Exception) {
                Log.e(TAG, "组织创建失败: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * 获取用户所在的组织列表
     * @param userId 用户 ID
     * @return 组织列表
     */
    suspend fun getOrganizations(userId: String): List<Organization> {
        return withContext(Dispatchers.IO) {
            try {
                val organizationsCollection = database.collection("organizations")
                val query = organizationsCollection.whereArrayContains("members", userId)
                val result = query.get()
                val organizations = mutableListOf<Organization>()
                
                for (doc in result.data) {
                    val data = doc.data
                    val organization = Organization(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        creatorId = data["creatorId"] as? String ?: "",
                        members = (data["members"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                    organizations.add(organization)
                }
                
                Log.d(TAG, "获取用户组织列表成功: $userId")
                organizations
            } catch (e: Exception) {
                Log.e(TAG, "获取用户组织列表失败: ${e.message}")
                emptyList()
            }
        }
    }
    
    /**
     * 获取组织内的所有成员
     * @param organizationId 组织 ID
     * @return 成员列表
     */
    suspend fun getOrganizationMembers(organizationId: String): List<User> {
        return withContext(Dispatchers.IO) {
            try {
                val organizationsCollection = database.collection("organizations")
                val orgResult = organizationsCollection.doc(organizationId).get()
                val orgData = orgResult.data
                
                val members = mutableListOf<User>()
                if (orgData != null) {
                    val memberIds = (orgData["members"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val usersCollection = database.collection("users")
                    
                    for (memberId in memberIds) {
                        try {
                            val userResult = usersCollection.doc(memberId).get()
                            val userData = userResult.data
                            if (userData != null) {
                                val user = User(
                                    id = userData["id"] as? String ?: "",
                                    name = userData["name"] as? String ?: "",
                                    email = userData["email"] as? String ?: ""
                                )
                                members.add(user)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "获取成员信息失败: ${e.message}")
                        }
                    }
                }
                
                Log.d(TAG, "获取组织成员列表成功: $organizationId")
                members
            } catch (e: Exception) {
                Log.e(TAG, "获取组织成员列表失败: ${e.message}")
                emptyList()
            }
        }
    }
    
    /**
     * 创建提醒
     * @param reminder 提醒对象
     * @return 创建成功后的提醒对象
     */
    suspend fun createReminder(reminder: Reminder): Reminder {
        return withContext(Dispatchers.IO) {
            try {
                val remindersCollection = database.collection("reminders")
                // Convert Reminder to Map for storage
                val reminderMap = mapOf(
                    "id" to reminder.id,
                    "orgId" to reminder.orgId,
                    "title" to reminder.title,
                    "description" to reminder.description,
                    "eventTime" to reminder.eventTime,
                    "location" to reminder.location,
                    "alertTimes" to reminder.alertTimes,
                    "participants" to reminder.participants,
                    "creator" to reminder.creator,
                    "createdAt" to reminder.createdAt,
                    "updatedAt" to reminder.updatedAt
                )
                val result = remindersCollection.add(reminderMap)
                val newReminder = reminder.copy(id = result.id)
                Log.d(TAG, "提醒创建成功: ${newReminder.id}")
                newReminder
            } catch (e: Exception) {
                Log.e(TAG, "提醒创建失败: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * 获取组织内的所有提醒
     * @param organizationId 组织 ID
     * @return 提醒列表
     */
    suspend fun getReminders(organizationId: String): List<Reminder> {
        return withContext(Dispatchers.IO) {
            try {
                val remindersCollection = database.collection("reminders")
                val query = remindersCollection.whereEqualTo("orgId", organizationId)
                val result = query.get()
                val reminders = mutableListOf<Reminder>()
                
                Log.d(TAG, "查询组织提醒列表: $organizationId, 结果数量: ${result.data.size}")
                
                for (doc in result.data) {
                    val data = doc.data
                    Log.d(TAG, "处理提醒文档: ${doc.id}, 数据: $data")
                    
                    val reminder = Reminder(
                        id = doc.id,
                        orgId = data["orgId"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        eventTime = data["eventTime"] as? Date ?: Date(),
                        location = data["location"] as? String ?: "",
                        alertTimes = (data["alertTimes"] as? List<*>)?.filterIsInstance<Date>() ?: emptyList(),
                        participants = (data["participants"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        creator = data["creator"] as? String ?: ""
                    )
                    reminders.add(reminder)
                }
                
                Log.d(TAG, "获取组织提醒列表成功: $organizationId, 共 ${reminders.size} 个提醒")
                reminders
            } catch (e: Exception) {
                Log.e(TAG, "获取组织提醒列表失败: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    /**
     * 获取用户的所有提醒
     * @param userId 用户 ID
     * @return 提醒列表
     */
    suspend fun getUserReminders(userId: String): List<Reminder> {
        return withContext(Dispatchers.IO) {
            try {
                val remindersCollection = database.collection("reminders")
                val query = remindersCollection.whereArrayContains("participants", userId)
                val result = query.get()
                val reminders = mutableListOf<Reminder>()
                
                Log.d(TAG, "查询用户提醒列表: $userId, 结果数量: ${result.data.size}")
                
                for (doc in result.data) {
                    val data = doc.data
                    Log.d(TAG, "处理提醒文档: ${doc.id}, 数据: $data")
                    
                    val reminder = Reminder(
                        id = doc.id,
                        orgId = data["orgId"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        eventTime = data["eventTime"] as? Date ?: Date(),
                        location = data["location"] as? String ?: "",
                        alertTimes = (data["alertTimes"] as? List<*>)?.filterIsInstance<Date>() ?: emptyList(),
                        participants = (data["participants"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        creator = data["creator"] as? String ?: ""
                    )
                    reminders.add(reminder)
                }
                
                Log.d(TAG, "获取用户提醒列表成功: $userId, 共 ${reminders.size} 个提醒")
                reminders
            } catch (e: Exception) {
                Log.e(TAG, "获取用户提醒列表失败: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    /**
     * 保存用户信息
     * @param user 用户对象
     * @return 保存结果，成功返回Result.success(Unit)，失败返回Result.failure(exception)
     */
    suspend fun saveUserResult(user: User): Result<Unit> {
        return try {
            saveUser(user)
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
    suspend fun getUserResult(userId: String): Result<User> {
        return try {
            val user = getUser(userId)
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
    suspend fun createOrganizationResult(name: String, creatorId: String): Result<Organization> {
        return try {
            val organization = Organization(
                name = name,
                creatorId = creatorId,
                members = listOf(creatorId)
            )
            val createdOrganization = createOrganization(organization)
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
    suspend fun joinOrganizationResult(userId: String, orgId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val organizationsCollection = database.collection("organizations")
                val orgResult = organizationsCollection.doc(orgId).get()
                val orgData = orgResult.data
                
                if (orgData != null) {
                    val members = (orgData["members"] as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                    if (!members.contains(userId)) {
                        members.add(userId)
                        organizationsCollection.doc(orgId).update(mapOf("members" to members))
                    }
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Organization not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 退出组织
     * @param userId 用户ID
     * @param orgId 组织ID
     * @return 退出结果，成功返回Result.success(Unit)，失败返回Result.failure(exception)
     */
    suspend fun leaveOrganizationResult(userId: String, orgId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val organizationsCollection = database.collection("organizations")
                val orgResult = organizationsCollection.doc(orgId).get()
                val orgData = orgResult.data
                
                if (orgData != null) {
                    val members = (orgData["members"] as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                    members.remove(userId)
                    organizationsCollection.doc(orgId).update(mapOf("members" to members))
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Organization not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 获取用户所属的组织列表
     * @param userId 用户ID
     * @return 组织列表，成功返回Result.success(List<Organization>)，失败返回Result.failure(exception)
     */
    suspend fun getOrganizationsByUserResult(userId: String): Result<List<Organization>> {
        return try {
            val organizations = getOrganizations(userId)
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
    suspend fun getOrganizationByIdResult(orgId: String): Result<Organization> {
        return withContext(Dispatchers.IO) {
            try {
                val organizationsCollection = database.collection("organizations")
                val result = organizationsCollection.doc(orgId).get()
                val data = result.data
                
                if (data != null) {
                    val organization = Organization(
                        id = orgId,
                        name = data["name"] as? String ?: "",
                        creatorId = data["creatorId"] as? String ?: "",
                        members = (data["members"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                    Result.success(organization)
                } else {
                    Result.failure(Exception("Organization not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 创建提醒
     * @param reminder 提醒对象
     * @return 创建结果，成功返回Result.success(Reminder)，失败返回Result.failure(exception)
     */
    suspend fun createReminderResult(reminder: Reminder): Result<Reminder> {
        return try {
            val createdReminder = createReminder(reminder)
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
    suspend fun updateReminderResult(reminder: Reminder): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val remindersCollection = database.collection("reminders")
                remindersCollection.doc(reminder.id).update(reminder)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 删除提醒
     * @param reminderId 提醒ID
     * @return 删除结果，成功返回Result.success(Unit)，失败返回Result.failure(exception)
     */
    suspend fun deleteReminderResult(reminderId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val remindersCollection = database.collection("reminders")
                remindersCollection.doc(reminderId).delete()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 获取组织的提醒列表
     * @param orgId 组织ID
     * @return 提醒列表，成功返回Result.success(List<Reminder>)，失败返回Result.failure(exception)
     */
    suspend fun getRemindersByOrganizationResult(orgId: String): Result<List<Reminder>> {
        return try {
            val reminders = getReminders(orgId)
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
    suspend fun getRemindersByUserResult(userId: String): Result<List<Reminder>> {
        return try {
            val reminders = getUserReminders(userId)
            Result.success(reminders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取组织列表
     * @return 组织列表流
     */
    fun getOrganizations(): Flow<List<Organization>> = flow {
        try {
            val organizationsCollection = database.collection("organizations")
            val result = organizationsCollection.get()
            val organizations = mutableListOf<Organization>()
            
            for (doc in result.data) {
                val data = doc.data
                val organization = Organization(
                    id = doc.id,
                    name = data["name"] as? String ?: "",
                    creatorId = data["creatorId"] as? String ?: "",
                    members = (data["members"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                )
                organizations.add(organization)
            }
            
            emit(organizations)
        } catch (e: Exception) {
            Log.e(TAG, "获取组织列表失败: ${e.message}")
            emit(emptyList())
        }
    }
    
    /**
     * 加入组织
     * @param organizationId 组织 ID
     * @param userId 用户 ID
     * @return 加入是否成功
     */
    suspend fun joinOrganization(organizationId: String, userId: String): Boolean {
        return try {
            val result = joinOrganizationResult(userId, organizationId)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "用户加入组织失败: ${e.message}")
            false
        }
    }
    
    /**
     * 获取组织的提醒列表
     * @param organizationId 组织 ID
     * @return 提醒列表流
     */
    fun getRemindersByOrganization(organizationId: String): Flow<List<Reminder>> = flow {
        try {
            val remindersCollection = database.collection("reminders")
            val query = remindersCollection.whereEqualTo("orgId", organizationId)
            val result = query.get()
            val reminders = mutableListOf<Reminder>()
            
            for (doc in result.data) {
                val data = doc.data
                val reminder = Reminder(
                    id = doc.id,
                    orgId = data["orgId"] as? String ?: "",
                    title = data["title"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    eventTime = data["eventTime"] as? Date ?: Date(),
                    location = data["location"] as? String ?: "",
                    alertTimes = (data["alertTimes"] as? List<*>)?.filterIsInstance<Date>() ?: emptyList(),
                    participants = (data["participants"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    creator = data["creator"] as? String ?: ""
                )
                reminders.add(reminder)
            }
            
            emit(reminders)
        } catch (e: Exception) {
            Log.e(TAG, "获取提醒列表失败: ${e.message}")
            emit(emptyList())
        }
    }
}
