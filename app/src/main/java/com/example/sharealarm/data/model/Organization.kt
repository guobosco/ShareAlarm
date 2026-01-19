package com.example.sharealarm.data.model

/**
 * 组织数据模型
 * 功能：表示一个用户组织，包含组织的基本信息和成员列表
 * @property id 组织唯一标识符
 * @property name 组织名称
 * @property creatorId 创建者ID
 * @property members 成员ID列表
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
data class Organization(
    val id: String = "",
    val name: String = "",
    val creatorId: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)