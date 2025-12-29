package com.example.rentr.repository

import com.example.rentr.model.TransactionModel
import com.google.firebase.database.FirebaseDatabase

class TransactionRepoImpl : TransactionRepo {

    private val database = FirebaseDatabase.getInstance()
    private val transactionsRef = database.getReference("transactions")

    override fun addTransaction(transaction: TransactionModel, callback: (Boolean, String?) -> Unit) {
        transactionsRef.child(transaction.transactionId).setValue(transaction.toMap())
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message)
            }
    }
}