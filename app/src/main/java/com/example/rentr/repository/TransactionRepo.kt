package com.example.rentr.repository

import com.example.rentr.model.TransactionModel

interface TransactionRepo {
    fun addTransaction(transaction: TransactionModel, callback: (Boolean, String?) -> Unit)
    fun getRenterTransactions(userId: String, callback: (Boolean, List<TransactionModel>) -> Unit)
    suspend fun getPidxFromBackend(amount: Double, purchaseOrderId: String, purchaseOrderName: String): String?
}