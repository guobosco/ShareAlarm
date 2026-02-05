package com.example.sharealarm.data.model

/**
 * 群组数据模型
 * @property id 群组唯一标识符
 * @property name 群组名称
 * @property memberIds 群组成员ID列表
 */
data class Group(
    val id: String,
    val name: String,
    val memberIds: List<String> = emptyList()
)
