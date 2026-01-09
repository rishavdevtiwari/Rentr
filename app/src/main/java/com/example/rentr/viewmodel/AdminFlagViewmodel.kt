package com.example.rentr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentr.model.NotificationModel
import com.example.rentr.model.ProductModel
import com.example.rentr.utils.FcmSenderV1
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.UUID

class AdminFlagViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseDatabase.getInstance()

    // 1. Resolve Flag (SENDS NOTIFICATION)
    fun resolveFlag(product: ProductModel) {
        val title = "Flag Resolved !!!"
        val body = "The flag on '${product.title}' has been removed. Your product is active."
        sendAndSaveNotification(product.listedBy, title, body)

        val updates = mapOf<String, Any?>(
            "flagged" to false,
            "flaggedBy" to emptyList<String>(),
            "flaggedReason" to emptyList<String>(),
            "appealReason" to "",
            "availability" to true
        )
        db.getReference("products").child(product.productId).updateChildren(updates)
        decrementFlagCount(product.listedBy)
    }

    // 2. Delete Product (SENDS NOTIFICATION)
    fun deleteProduct(product: ProductModel) {
        val title = "Product Deleted !!!"
        val body = "Your product '${product.title}' was deleted due to policy violations."
        sendAndSaveNotification(product.listedBy, title, body)

        db.getReference("products").child(product.productId).removeValue()
    }

    // 3. Mark for Review (SILENT - NO NOTIFICATION)
    fun markForReview(product: ProductModel) {
        // Just update DB to hide product
        val updates = mapOf<String, Any?>(
            "availability" to false, // Hides it from search
            "flagged" to true
        )
        db.getReference("products").child(product.productId).updateChildren(updates)
    }

    // 4. Delete User Account (SENDS NOTIFICATION)
    fun deleteUserAccount(userId: String) {
        val title = "Account Suspended !!!"
        val body = "Your account has been permanently suspended due to violations."
        sendAndSaveNotification(userId, title, body)

        db.getReference("users").child(userId).removeValue()
    }

    private fun decrementFlagCount(userId: String) {
        val userRef = db.getReference("users").child(userId)
        userRef.child("flagCount").get().addOnSuccessListener {
            val current = it.value.toString().toIntOrNull() ?: 0
            if (current > 0) userRef.child("flagCount").setValue(current - 1)
        }
    }

    private fun sendAndSaveNotification(userId: String, title: String, body: String) {
        val notifId = UUID.randomUUID().toString()
        val notification = NotificationModel(notifId, title, body, System.currentTimeMillis(), false)
        db.getReference("notifications").child(userId).child(notifId).setValue(notification)

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