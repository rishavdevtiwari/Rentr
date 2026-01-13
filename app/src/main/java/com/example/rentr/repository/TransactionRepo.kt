package com.example.rentr.repository

import com.example.rentr.model.TransactionModel

interface TransactionRepo {
    fun addTransaction(transaction: TransactionModel, callback: (Boolean, String?) -> Unit)
    fun getRenterTransactions(userId: String, callback: (Boolean, List<TransactionModel>) -> Unit)
    fun getSellerTransactions(userId: String, callback: (Boolean, List<TransactionModel>) -> Unit)
    fun getTransactionById(transactionId: String, callback: (Boolean, TransactionModel?) -> Unit)
    fun updateTransaction(transactionId: String, updates: Map<String, Any>, callback: (Boolean, String?) -> Unit)
    suspend fun getPidxFromBackend(amount: Double, purchaseOrderId: String, purchaseOrderName: String): String?
}