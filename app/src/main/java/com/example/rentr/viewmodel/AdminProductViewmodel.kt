package com.example.rentr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentr.utils.FcmSenderV1
import com.google.firebase.database.FirebaseDatabase // Using Realtime Database only
import kotlinx.coroutines.launch

class AdminProductViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize Realtime Database
    private val db = FirebaseDatabase.getInstance()

    // 1. Approve Logic: Notify User -> Update Product in RTDB
    fun approveProduct(productId: String, ownerId: String) {
        viewModelScope.launch {
            // A. Get the user's Token from Realtime Database
            val userRef = db.getReference("users").child(ownerId)

            userRef.get().addOnSuccessListener { snapshot ->
                val token = snapshot.child("fcmToken").value as? String

                if (token != null) {
                    // Send "Approved" Notification
                    val sender = FcmSenderV1(
                        context = getApplication(),
                        userToken = token,
                        title = "Product Verified! ✅",
                        body = "Your listing is now live on Rentr."
                    )
                    viewModelScope.launch { sender.send() }
                }

                // B. Update Product Status in Realtime Database
                val productUpdates = mapOf<String, Any>(
                    "verified" to true,
                    "status" to "verified"
                )

                db.getReference("products").child(productId)
                    .updateChildren(productUpdates)
            }
        }
    }

    // 2. Reject Logic: Notify User -> Update Product in RTDB
    fun rejectProduct(productId: String, ownerId: String, reason: String) {
        viewModelScope.launch {
            // A. Get Token from Realtime Database
            val userRef = db.getReference("users").child(ownerId)

            userRef.get().addOnSuccessListener { snapshot ->
                val token = snapshot.child("fcmToken").value as? String

                if (token != null) {
                    // Send "Rejected" Notification
                    val sender = FcmSenderV1(
                        context = getApplication(),
                        userToken = token,
                        title = "Product Rejected ❌",
                        body = "Reason: $reason"
                    )
                    viewModelScope.launch { sender.send() }
                }

                // B. Update Product Status in Realtime Database
                val productUpdates = mapOf<String, Any>(
                    "verified" to false,
                    "status" to "rejected",
                    "rejectionReason" to reason
                )

                db.getReference("products").child(productId)
                    .updateChildren(productUpdates)
            }
        }
    }
}