package com.example.rentr.repository

import android.content.Context
import com.example.rentr.model.NotificationModel
import com.example.rentr.utils.FcmSenderV1
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class NotificationRepoImpl(private val context: Context) : NotificationRepo {

    private val db = FirebaseDatabase.getInstance()
    private val notificationsRef = db.getReference("notifications")
    private val usersRef = db.getReference("users")

    override fun sendAndSaveNotification(
        userId: String,
        title: String,
        body: String,
        callback: (Boolean, String) -> Unit
    ) {
        val notifId = UUID.randomUUID().toString()

        // FIX: Remove "notifId =" and other labels to use positional arguments
        val notification = NotificationModel(
            notifId,
            title,
            body,
            System.currentTimeMillis(),
            false
        )

        // 1. Save to Database
        notificationsRef.child(userId).child(notifId).setValue(notification)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 2. Send FCM Notification
                    sendFcm(userId, title, body)
                    callback(true, "Notification saved and sent")
                } else {
                    callback(false, task.exception?.message ?: "Failed to save notification")
                }
            }
    }

    private fun sendFcm(userId: String, title: String, body: String) {
        usersRef.child(userId).child("fcmToken").get().addOnSuccessListener { snapshot ->
            val token = snapshot.value as? String
            if (!token.isNullOrEmpty()) {
                // Launch in IO scope since FcmSenderV1 contains network calls
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        FcmSenderV1(context, token, title, body).send()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}