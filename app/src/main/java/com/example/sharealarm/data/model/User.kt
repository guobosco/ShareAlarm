package com.example.sharealarm.data.model

/**
 * 用户数据模型
 * 功能：表示一个应用用户，包含用户的基本信息和认证相关数据
 * @property id 用户唯一标识符
 * @property name 用户姓名
 * @property email 用户邮箱
 * @property photoUrl 用户头像URL
 * @property createdAt 用户创建时间
 * @property updatedAt 用户信息更新时间
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val tags: List<String> = emptyList(), // 用户标签
    val shareAlarmId: String = "", // 飞铃号
    val remarkName: String = "", // 备注名
    val phoneNumber: String = "", // 电话
    val memo: String = "", // 备忘
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)