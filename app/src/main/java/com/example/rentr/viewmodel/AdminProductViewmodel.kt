package com.example.rentr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentr.repository.NotificationRepo
import com.example.rentr.repository.ProductRepo

class AdminProductViewModel(
    private val productRepo: ProductRepo,
    private val notifRepo: NotificationRepo
) : ViewModel() {

    fun approveProduct(productId: String, ownerId: String) {
        // FIX: Explicitly pass 'null' as the 3rd argument (reason)
        productRepo.updateProductVerification(productId, true, null) { success, _ ->
            if (success) {
                val title = "Product Verified !!!"
                val body = "Your listing is now live on Rentr."
                notifRepo.sendAndSaveNotification(ownerId, title, body) { _, _ -> }
            }
        }
    }

    fun rejectProduct(productId: String, ownerId: String, reason: String) {
        // FIX: Explicitly pass 'reason' as the 3rd argument
        productRepo.updateProductVerification(productId, false, reason) { success, _ ->
            if (success) {
                val title = "Product Rejected !!!"
                val body = "Reason: $reason"
                notifRepo.sendAndSaveNotification(ownerId, title, body) { _, _ -> }
            }
        }
    }

    class Factory(
        private val productRepo: ProductRepo,
        private val notifRepo: NotificationRepo
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminProductViewModel(productRepo, notifRepo) as T
        }
    }
}