package com.example.rentr.model

data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)