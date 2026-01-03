package com.example.rentr.repository

import com.example.rentr.model.TransactionModel

interface TransactionRepo {
    fun addTransaction(transaction: TransactionModel, callback: (Boolean, String?) -> Unit)
}