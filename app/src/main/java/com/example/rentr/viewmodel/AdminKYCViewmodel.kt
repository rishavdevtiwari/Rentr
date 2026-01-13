package com.example.rentr.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentr.model.NotificationModel
import com.example.rentr.utils.FcmSenderV1
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.UUID

class AdminKYCViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseDatabase.getInstance()

    // 1. Approve KYC
    fun approveKYC(userId: String) {
        val title = "KYC Verified !!!"
        val body = "Your identity verification is complete. You now have full access."

        // A. Send & Save Notification
        sendAndSaveNotification(userId, title, body)

        // B. Update User Status in Database
        val updates = mapOf<String, Any>(
            "verified" to true
        )
        db.getReference("users").child(userId).updateChildren(updates)
    }


    fun rejectKYC(userId: String, reason: String) {
        val title = "KYC Rejected !!!"
        val body = "Reason: $reason. Please upload valid documents."


        sendAndSaveNotification(userId, title, body)


        val updates = mapOf<String, Any>(
            "verified" to false,
            "kycUrl" to emptyList<String>()
        )
        db.getReference("users").child(userId).updateChildren(updates)
    }


    private fun sendAndSaveNotification(userId: String, title: String, body: String) {
        // 1. Save to Notification History (Database)
        val notifId = UUID.randomUUID().toString()
        val notification = NotificationModel(notifId, title, body, System.currentTimeMillis(), false)
        db.getReference("notifications").child(userId).child(notifId).setValue(notification)

        // 2. Send FCM Push Notification (Pop-up)
        db.getReference("users").child(userId).child("fcmToken").get().addOnSuccessListener { snapshot ->
            val token = snapshot.value as? String
            if (!token.isNullOrEmpty()) {
                viewModelScope.launch {
                    val sender = FcmSenderV1(getApplication(), token, title, body)
                    sender.send()
                }
            }
        }
    }
}