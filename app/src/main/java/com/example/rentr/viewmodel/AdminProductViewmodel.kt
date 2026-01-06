package com.example.rentr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentr.utils.FcmSenderV1
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AdminProductViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()

    // 1. Approve Logic: Notify User -> Update DB
    fun approveProduct(productId: String, ownerId: String) {
        viewModelScope.launch {
            // A. Get the user's Token
            db.collection("users").document(ownerId).get()
                .addOnSuccessListener { document ->
                    val token = document.getString("fcmToken")

                    if (token != null) {
                        // B. Send "Approved" Notification
                        val sender = FcmSenderV1(
                            context = getApplication(),
                            userToken = token,
                            title = "Product Verified! ✅",
                            body = "Your listing is now live on Rentr."
                        )
                        viewModelScope.launch { sender.send() }
                    }

                    // C. Update Firestore
                    db.collection("products").document(productId)
                        .update("status", "verified", "verified", true)
                }
        }
    }

    // 2. Reject Logic: Notify User -> Update DB with Reason
    fun rejectProduct(productId: String, ownerId: String, reason: String) {
        viewModelScope.launch {
            // A. Get Token
            db.collection("users").document(ownerId).get()
                .addOnSuccessListener { document ->
                    val token = document.getString("fcmToken")

                    if (token != null) {
                        // B. Send "Rejected" Notification
                        val sender = FcmSenderV1(
                            context = getApplication(),
                            userToken = token,
                            title = "Product Rejected ❌",
                            body = "Reason: $reason"
                        )
                        viewModelScope.launch { sender.send() }
                    }

                    // C. Update Firestore
                    db.collection("products").document(productId)
                        .update(
                            "verified", false,
                            "status", "rejected",
                            "rejectionReason", reason
                        )
                }
        }
    }
}