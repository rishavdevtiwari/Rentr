package com.example.rentr.repository

import com.example.rentr.model.TransactionModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    // --- NEW FUNCTION TO CALL BACKEND ---
    override suspend fun getPidxFromBackend(
        amount: Double,
        purchaseOrderId: String,
        purchaseOrderName: String
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Convert Rs to Paisa (Khalti expects integer Paisa)
                val amountInPaisa = (amount * 100).toLong()

                // 2. Prepare the request object
                val request = BackendRequest(
                    amount = amountInPaisa,
                    purchaseOrderId = purchaseOrderId,
                    purchaseOrderName = purchaseOrderName
                )

                // 3. Call the Node.js backend via Retrofit
                val response = RetrofitClient.api.getPidx(request)

                // 4. Return the pidx string
                response.pidx

            } catch (e: Exception) {
                e.printStackTrace()
                null // Return null if backend is offline or errors
            }
        }
    }
}