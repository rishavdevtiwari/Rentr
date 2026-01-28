package com.example.rentr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.NotificationRepo
import com.example.rentr.repository.ProductRepo
import com.example.rentr.repository.UserRepo

class AdminFlagViewModel(
    private val productRepo: ProductRepo,
    private val userRepo: UserRepo,
    private val notifRepo: NotificationRepo
) : ViewModel() {

    // 1. Mark for Review
    fun markForReview(product: ProductModel) {
        // A. Mark product in DB
        productRepo.markProductForReview(product.productId) { success, _ ->
            if (success) {
                // B. Increment Seller Flag Count
                userRepo.incrementFlagCount(product.listedBy) { _, _ -> }

                // C. Send Notification
                val title = "Product Under Review"
                val body = "Your product '${product.title}' is under review by admins."
                notifRepo.sendAndSaveNotification(product.listedBy, title, body) { _, _ -> }
            }
        }
    }

    // 2. Delete Product
    fun deleteProduct(product: ProductModel) {
        // A. Increment Seller Flag Count (Done first to ensure it happens before product deletion issues)
        userRepo.incrementFlagCount(product.listedBy) { _, _ -> }

        // B. Delete Product
        productRepo.deleteProduct(product.productId) { success, _ ->
            if (success) {
                // C. Send Notification
                val title = "Product Deleted !!!"
                val body = "Your product '${product.title}' was deleted due to policy violations."
                notifRepo.sendAndSaveNotification(product.listedBy, title, body) { _, _ -> }
            }
        }
    }

    // 3. Resolve Flag
    fun resolveFlag(product: ProductModel) {
        productRepo.clearFlags(product.productId) { success, _ ->
            if (success) {
                val title = "Flag Resolved !!!"
                val body = "The flag on '${product.title}' has been removed. Your product is active."
                notifRepo.sendAndSaveNotification(product.listedBy, title, body) { _, _ -> }
            }
        }
    }

    // 4. Delete User Account
    fun deleteUserAccount(userId: String) {
        // Send notification FIRST because once account is deleted, we might lose token access or permission
        val title = "Account Suspended !!!"
        val body = "Your account has been permanently suspended due to violations."
        notifRepo.sendAndSaveNotification(userId, title, body) { _, _ -> }

        userRepo.deleteAccount(userId) { _, _ -> }
    }

    // Factory to inject Repos
    class Factory(
        private val productRepo: ProductRepo,
        private val userRepo: UserRepo,
        private val notifRepo: NotificationRepo
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminFlagViewModel(productRepo, userRepo, notifRepo) as T
        }
    }
}