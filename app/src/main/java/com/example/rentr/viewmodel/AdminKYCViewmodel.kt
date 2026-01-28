package com.example.rentr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentr.repository.NotificationRepo
import com.example.rentr.repository.UserRepo

class AdminKYCViewModel(
    private val userRepo: UserRepo,
    private val notifRepo: NotificationRepo
) : ViewModel() {

    fun approveKYC(userId: String) {
        userRepo.verifyUserKYC(userId, true) { success, _ ->
            if (success) {
                val title = "KYC Verified !!!"
                val body = "Your identity verification is complete. You now have full access."
                notifRepo.sendAndSaveNotification(userId, title, body) { _, _ -> }
            }
        }
    }

    fun rejectKYC(userId: String, reason: String) {
        userRepo.verifyUserKYC(userId, false) { success, _ ->
            if (success) {
                val title = "KYC Rejected !!!"
                val body = "Reason: $reason. Please upload valid documents."
                notifRepo.sendAndSaveNotification(userId, title, body) { _, _ -> }
            }
        }
    }

    class Factory(
        private val userRepo: UserRepo,
        private val notifRepo: NotificationRepo
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminKYCViewModel(userRepo, notifRepo) as T
        }
    }
}