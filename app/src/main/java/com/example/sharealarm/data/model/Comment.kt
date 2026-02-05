package com.example.sharealarm.data.model

import java.util.Date

data class Comment(
    val id: String,
    val userId: String,
    val userName: String,
    val content: String,
    val timestamp: Date,
    val isEmoji: Boolean = false
)
