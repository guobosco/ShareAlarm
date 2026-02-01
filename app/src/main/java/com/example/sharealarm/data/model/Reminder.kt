package com.example.sharealarm.data.model

import java.util.Date

/**
 * 提醒数据模型
 * 功能：表示一个共享提醒，包含提醒的基本信息、事件时间、提醒时间列表和提醒人员
 * @property id 提醒唯一标识符
 * @property orgId 所属组织ID
 * @property title 事件名称
 * @property description 事件描述
 * @property eventTime 事件时间
 * @property location 事件地点
 * @property alertTimes 提醒时间列表
 * @property participants 提醒人员ID列表
 * @property creator 创建者ID
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
data class Reminder(
    val id: String = "",
    val orgId: String = "",
    val title: String = "",
    val description: String = "",
    val eventTime: Date = Date(),
    val location: String = "",
    val alertTimes: List<Date> = emptyList(),
    val participants: List<String> = emptyList(),
    val creator: String = "",
    val isRead: Boolean = false, // 是否已读
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)