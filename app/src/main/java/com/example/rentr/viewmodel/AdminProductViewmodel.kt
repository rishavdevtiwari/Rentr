package com.example.rentr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentr.model.NotificationModel
import com.example.rentr.utils.FcmSenderV1
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.UUID

class AdminProductViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseDatabase.getInstance()

    fun approveProduct(productId: String, ownerId: String) {
        val title = "Product Verified !!!"
        val body = "Your listing is now live on Rentr."

        // 1. Send & Save Notification
        sendAndSaveNotification(ownerId, title, body)

        // 2. Update Product
        db.getReference("products").child(productId).updateChildren(
            mapOf("verified" to true, "rejectionReason" to null)
        )
    }

    fun rejectProduct(productId: String, ownerId: String, reason: String) {
        val title = "Product Rejected !!!"
        val body = "Reason: $reason"

        // 1. Send & Save Notification
        sendAndSaveNotification(ownerId, title, body)

        // 2. Update Product
        db.getReference("products").child(productId).updateChildren(
            mapOf("verified" to false, "rejectionReason" to reason)
        )
    }

    private fun sendAndSaveNotification(userId: String, title: String, body: String) {
        // A. Save to Database (History)
        val notifId = UUID.randomUUID().toString()
        val notification = NotificationModel(notifId, title, body, System.currentTimeMillis(), false)

        db.getReference("notifications").child(userId).child(notifId).setValue(notification)

        // B. Send FCM (Pop-up)
        db.getReference("users").child(userId).child("fcmToken").get().addOnSuccessListener {
            val token = it.value as? String
            if (!token.isNullOrEmpty()) {
                viewModelScope.launch {
                    FcmSenderV1(getApplication(), token, title, body).send()
                }
            }
        }
    }
}