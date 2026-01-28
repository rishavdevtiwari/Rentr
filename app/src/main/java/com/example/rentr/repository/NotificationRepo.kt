package com.example.rentr.repository

interface NotificationRepo {
    fun sendAndSaveNotification(
        userId: String,
        title: String,
        body: String,
        callback: (Boolean, String) -> Unit
    )
}