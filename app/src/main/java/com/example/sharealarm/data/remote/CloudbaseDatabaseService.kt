package com.example.sharealarm.data.remote

import com.example.sharealarm.data.model.Organization
import com.example.sharealarm.data.model.Reminder
import com.example.sharealarm.data.model.User
import com.tencent.cloudbase.database.CloudbaseDatabase
import com.tencent.cloudbase.database.Document
import com.tencent.cloudbase.database.Query
import com.tencent.cloudbase.database.Where
import com.tencent.cloudbase.database.enums.QueryCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cloudbase 数据库服务
 * 功能：封装 Cloudbase 数据库相关的操作，包括用户、组织和提醒的增删改查
 * @property db Cloudbase 数据库实例
 */
class CloudbaseDatabaseService(private val db: CloudbaseDatabase) {

    // 集合名称常量
    private val USERS_COLLECTION = "users"
    private val ORGANIZATIONS_COLLECTION = "organizations"
    private val REMINDERS_COLLECTION = "reminders"

    /**
     * 保存用户信息到数据库
     * @param user 用户对象
     */
    suspend fun saveUser(user: User) {
        return withContext(Dispatchers.IO) {
            val usersCollection = db.collection(USERS_COLLECTION)
            usersCollection.document(user.id).set(user)
        }
    }

    /**
     * 从数据库获取用户信息
     * @param userId 用户 ID
     * @return 用户对象，如果不存在则返回 null
     */
    suspend fun getUser(userId: String): User? {
        return withContext(Dispatchers.IO) {
            val usersCollection = db.collection(USERS_COLLECTION)
            val result = usersCollection.document(userId).get()
            val document = result.data
            return@withContext document?.let {
                User(
                    id = it["id"] as String,
                    name = it["name"] as String,
                    email = it["email"] as String
                )
            }
        }
    }

    /**
     * 创建组织
     * @param organization 组织对象
     * @return 创建成功后的组织对象
     */
    suspend fun createOrganization(organization: Organization): Organization {
        return withContext(Dispatchers.IO) {
            val organizationsCollection = db.collection(ORGANIZATIONS_COLLECTION)
            val result = organizationsCollection.add(organization)
            val newOrganization = organization.copy(id = result.id)
            organizationsCollection.document(result.id).set(newOrganization)
            return@withContext newOrganization
        }
    }

    /**
     * 获取用户所在的组织列表
     * @param userId 用户 ID
     * @return 组织列表
     */
    suspend fun getOrganizations(userId: String): List<Organization> {
        return withContext(Dispatchers.IO) {
            val organizationsCollection = db.collection(ORGANIZATIONS_COLLECTION)
            val query = organizationsCollection.where(
                Where(
                    "members",
                    QueryCommand.ARRAY_CONTAINS,
                    userId
                )
            )
            val result = query.get()
            return@withContext result.data.mapNotNull { document ->
                Organization(
                    id = document["id"] as String,
                    name = document["name"] as String,
                    creatorId = document["creatorId"] as String,
                    members = document["members"] as List<String>,
                    createdAt = document["createdAt"] as? Long ?: 0
                )
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
            val organizationsCollection = db.collection(ORGANIZATIONS_COLLECTION)
            val orgDoc = organizationsCollection.document(organizationId).get().data
            val memberIds = orgDoc?.get("members") as? List<String> ?: emptyList()
            
            val members = mutableListOf<User>()
            val usersCollection = db.collection(USERS_COLLECTION)
            
            for (memberId in memberIds) {
                val userDoc = usersCollection.document(memberId).get().data
                userDoc?.let {
                    members.add(
                        User(
                            id = it["id"] as String,
                            name = it["name"] as String,
                            email = it["email"] as String
                        )
                    )
                }
            }
            
            return@withContext members
        }
    }

    /**
     * 创建提醒
     * @param reminder 提醒对象
     * @return 创建成功后的提醒对象
     */
    suspend fun createReminder(reminder: Reminder): Reminder {
        return withContext(Dispatchers.IO) {
            val remindersCollection = db.collection(REMINDERS_COLLECTION)
            val result = remindersCollection.add(reminder)
            val newReminder = reminder.copy(id = result.id)
            remindersCollection.document(result.id).set(newReminder)
            return@withContext newReminder
        }
    }

    /**
     * 获取组织内的所有提醒
     * @param organizationId 组织 ID
     * @return 提醒列表
     */
    suspend fun getReminders(organizationId: String): List<Reminder> {
        return withContext(Dispatchers.IO) {
            val remindersCollection = db.collection(REMINDERS_COLLECTION)
            val query = remindersCollection.where(
                Where(
                    "organizationId",
                    QueryCommand.EQ,
                    organizationId
                )
            )
            val result = query.get()
            return@withContext result.data.mapNotNull { document ->
                Reminder(
                    id = document["id"] as String,
                    title = document["title"] as String,
                    description = document["description"] as? String ?: "",
                    time = document["time"] as Long,
                    isRepeated = document["isRepeated"] as Boolean,
                    repeatDays = document["repeatDays"] as? List<Int> ?: emptyList(),
                    organizationId = document["organizationId"] as String,
                    creatorId = document["creatorId"] as String,
                    reminderUsers = document["reminderUsers"] as List<String>,
                    createdAt = document["createdAt"] as? Long ?: 0
                )
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
            val remindersCollection = db.collection(REMINDERS_COLLECTION)
            val query = remindersCollection.where(
                Where(
                    "reminderUsers",
                    QueryCommand.ARRAY_CONTAINS,
                    userId
                )
            )
            val result = query.get()
            return@withContext result.data.mapNotNull { document ->
                Reminder(
                    id = document["id"] as String,
                    title = document["title"] as String,
                    description = document["description"] as? String ?: "",
                    time = document["time"] as Long,
                    isRepeated = document["isRepeated"] as Boolean,
                    repeatDays = document["repeatDays"] as? List<Int> ?: emptyList(),
                    organizationId = document["organizationId"] as String,
                    creatorId = document["creatorId"] as String,
                    reminderUsers = document["reminderUsers"] as List<String>,
                    createdAt = document["createdAt"] as? Long ?: 0
                )
            }
        }
    }

    /**
     * 保存用户信息
     * @param user 用户对象
     * @return 保存结果，成功返回Result.success(Unit)，失败返回Result.failure(exception)
     */
    suspend fun saveUserResult(user: User): Result<Unit> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val usersCollection = db.collection(USERS_COLLECTION)
                usersCollection.document(user.id).set(user)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息，成功返回Result.success(User)，失败返回Result.failure(exception)
     */
    suspend fun getUserResult(userId: String): Result<User> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val usersCollection = db.collection(USERS_COLLECTION)
                val result = usersCollection.document(userId).get()
                val document = result.data
                val user = document?.let {
                    User(
                        id = it["id"] as String,
                        name = it["name"] as String,
                        email = it["email"] as String
                    )
                }
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 创建组织
     * @param name 组织名称
     * @param creatorId 创建者ID
     * @return 创建结果，成功返回Result.success(Organization)，失败返回Result.failure(exception)
     */
    suspend fun createOrganizationResult(name: String, creatorId: String): Result<Organization> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val organization = Organization(
                    name = name,
                    creatorId = creatorId,
                    members = listOf(creatorId)
                )
                val organizationsCollection = db.collection(ORGANIZATIONS_COLLECTION)
                val result = organizationsCollection.add(organization)
                val createdOrganization = organization.copy(id = result.id)
                organizationsCollection.document(result.id).set(createdOrganization)
                Result.success(createdOrganization)
            } catch (e: Exception) {
                Result.failure(e)
            }
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
            return@withContext try {
                val organizationsCollection = db.collection(ORGANIZATIONS_COLLECTION)
                val orgDoc = organizationsCollection.document(orgId).get()
                val organizationData = orgDoc.data
                
                if (organizationData != null) {
                    val members = organizationData["members"] as? MutableList<String> ?: mutableListOf()
                    if (!members.contains(userId)) {
                        members.add(userId)
                        organizationsCollection.document(orgId)
                            .update(mapOf("members" to members))
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
            return@withContext try {
                val organizationsCollection = db.collection(ORGANIZATIONS_COLLECTION)
                val orgDoc = organizationsCollection.document(orgId).get()
                val organizationData = orgDoc.data
                
                if (organizationData != null) {
                    val members = organizationData["members"] as? MutableList<String> ?: mutableListOf()
                    members.remove(userId)
                    organizationsCollection.document(orgId)
                        .update(mapOf("members" to members))
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
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val organizationsCollection = db.collection(ORGANIZATIONS_COLLECTION)
                val query = organizationsCollection.where(
                    Where(
                        "members",
                        QueryCommand.ARRAY_CONTAINS,
                        userId
                    )
                )
                val result = query.get()
                val organizations = result.data.mapNotNull { document ->
                    Organization(
                        id = document["id"] as String,
                        name = document["name"] as String,
                        creatorId = document["creatorId"] as String,
                        members = document["members"] as List<String>,
                        createdAt = document["createdAt"] as? Long ?: 0
                    )
                }
                Result.success(organizations)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取组织详情
     * @param orgId 组织ID
     * @return 组织对象，成功返回Result.success(Organization)，失败返回Result.failure(exception)
     */
    suspend fun getOrganizationByIdResult(orgId: String): Result<Organization> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val organizationsCollection = db.collection(ORGANIZATIONS_COLLECTION)
                val result = organizationsCollection.document(orgId).get()
                val document = result.data
                val organization = document?.let {
                    Organization(
                        id = it["id"] as String,
                        name = it["name"] as String,
                        creatorId = it["creatorId"] as String,
                        members = it["members"] as List<String>,
                        createdAt = it["createdAt"] as? Long ?: 0
                    )
                }
                if (organization != null) {
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
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val remindersCollection = db.collection(REMINDERS_COLLECTION)
                val result = remindersCollection.add(reminder)
                val createdReminder = reminder.copy(id = result.id)
                remindersCollection.document(result.id).set(createdReminder)
                Result.success(createdReminder)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 更新提醒
     * @param reminder 提醒对象
     * @return 更新结果，成功返回Result.success(Unit)，失败返回Result.failure(exception)
     */
    suspend fun updateReminderResult(reminder: Reminder): Result<Unit> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val remindersCollection = db.collection(REMINDERS_COLLECTION)
                remindersCollection.document(reminder.id).set(reminder)
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
            return@withContext try {
                val remindersCollection = db.collection(REMINDERS_COLLECTION)
                remindersCollection.document(reminderId).delete()
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
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val remindersCollection = db.collection(REMINDERS_COLLECTION)
                val query = remindersCollection.where(
                    Where(
                        "organizationId",
                        QueryCommand.EQ,
                        orgId
                    )
                )
                val result = query.get()
                val reminders = result.data.mapNotNull { document ->
                    Reminder(
                        id = document["id"] as String,
                        title = document["title"] as String,
                        description = document["description"] as? String ?: "",
                        time = document["time"] as Long,
                        isRepeated = document["isRepeated"] as Boolean,
                        repeatDays = document["repeatDays"] as? List<Int> ?: emptyList(),
                        organizationId = document["organizationId"] as String,
                        creatorId = document["creatorId"] as String,
                        reminderUsers = document["reminderUsers"] as List<String>,
                        createdAt = document["createdAt"] as? Long ?: 0
                    )
                }
                Result.success(reminders)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取用户的提醒列表
     * @param userId 用户ID
     * @return 提醒列表，成功返回Result.success(List<Reminder>)，失败返回Result.failure(exception)
     */
    suspend fun getRemindersByUserResult(userId: String): Result<List<Reminder>> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val remindersCollection = db.collection(REMINDERS_COLLECTION)
                val query = remindersCollection.where(
                    Where(
                        "reminderUsers",
                        QueryCommand.ARRAY_CONTAINS,
                        userId
                    )
                )
                val result = query.get()
                val reminders = result.data.mapNotNull { document ->
                    Reminder(
                        id = document["id"] as String,
                        title = document["title"] as String,
                        description = document["description"] as? String ?: "",
                        time = document["time"] as Long,
                        isRepeated = document["isRepeated"] as Boolean,
                        repeatDays = document["repeatDays"] as? List<Int> ?: emptyList(),
                        organizationId = document["organizationId"] as String,
                        creatorId = document["creatorId"] as String,
                        reminderUsers = document["reminderUsers"] as List<String>,
                        createdAt = document["createdAt"] as? Long ?: 0
                    )
                }
                Result.success(reminders)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}