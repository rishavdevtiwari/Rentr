package com.example.rentr.repository

import com.example.rentr.model.TransactionModel

interface TransactionRepo {
    fun addTransaction(transaction: TransactionModel, callback: (Boolean, String?) -> Unit)

    // ADD THIS NEW LINE so the ViewModel and RepoImpl can communicate
    suspend fun getPidxFromBackend(amount: Double, purchaseOrderId: String, purchaseOrderName: String): String?
}