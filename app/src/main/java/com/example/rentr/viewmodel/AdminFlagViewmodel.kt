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
    private val productsRef = db.getReference("products")
    private val usersRef = db.getReference("users")

    // Helper function to increment user flag count
    private fun incrementUserFlagCount(userId: String) {
        usersRef.child(userId).child("flagCount").get().addOnSuccessListener { snapshot ->
            val currentCount = (snapshot.value as? Long)?.toInt() ?: 0
            usersRef.child(userId).child("flagCount").setValue(currentCount + 1)
        }
    }

    // 1. Mark for Review (SENDS NOTIFICATION + increments user flag count)
    fun markForReview(product: ProductModel) {
        // Update product: set flagged=true and hide from listings
        val updates = mapOf<String, Any>(
            "flagged" to true,
            "availability" to false
        )
        productsRef.child(product.productId).updateChildren(updates)

        // Increment the seller's flag count
        incrementUserFlagCount(product.listedBy)

        // Send notification
        val title = "Product Under Review"
        val body = "Your product '${product.title}' is under review by admins."
        sendAndSaveNotification(product.listedBy, title, body)
    }

    // 2. Delete Product (SENDS NOTIFICATION + increments user flag count)
    fun deleteProduct(product: ProductModel) {
        // Increment flag count first
        incrementUserFlagCount(product.listedBy)

        // Delete product
        productsRef.child(product.productId).removeValue()

        // Send notification
        val title = "Product Deleted !!!"
        val body = "Your product '${product.title}' was deleted due to policy violations."
        sendAndSaveNotification(product.listedBy, title, body)
    }

    // 3. Resolve Flag (SENDS NOTIFICATION - NO change to user flag count)
    fun resolveFlag(product: ProductModel) {
        // Clear all flag data but DON'T affect user flag count
        val updates = mapOf<String, Any>(
            "flagged" to false,
            "flaggedBy" to emptyList<String>(),
            "flaggedReason" to emptyList<String>(),
            "appealReason" to "",
            "availability" to true
        )

        productsRef.child(product.productId).updateChildren(updates)

        // Send notification
        val title = "Flag Resolved !!!"
        val body = "The flag on '${product.title}' has been removed. Your product is active."
        sendAndSaveNotification(product.listedBy, title, body)

        // NO change to user flag count for resolution
    }

    // 4. Delete User Account (SENDS NOTIFICATION)
    fun deleteUserAccount(userId: String) {
        val title = "Account Suspended !!!"
        val body = "Your account has been permanently suspended due to violations."
        sendAndSaveNotification(userId, title, body)

        usersRef.child(userId).removeValue()
    }

    private fun sendAndSaveNotification(userId: String, title: String, body: String) {
        val notifId = UUID.randomUUID().toString()
        val notification = NotificationModel(notifId, title, body, System.currentTimeMillis(), false)
        db.getReference("notifications").child(userId).child(notifId).setValue(notification)

        usersRef.child(userId).child("fcmToken").get().addOnSuccessListener {
            val token = it.value as? String
            if (!token.isNullOrEmpty()) {
                viewModelScope.launch {
                    FcmSenderV1(getApplication(), token, title, body).send()
                }
            }
        }
    }
}