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

    override fun getRenterTransactions(userId: String, callback: (Boolean, List<TransactionModel>) -> Unit) {
        transactionsRef.orderByChild("renterId").equalTo(userId)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(TransactionModel::class.java) }
                    callback(true, list)
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    callback(false, emptyList())
                }
            })
    }

    override fun getSellerTransactions(userId: String, callback: (Boolean, List<TransactionModel>) -> Unit) {
        transactionsRef.orderByChild("sellerId").equalTo(userId)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(TransactionModel::class.java) }
                    callback(true, list)
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    callback(false, emptyList())
                }
            })
    }

    override fun getTransactionById(transactionId: String, callback: (Boolean, TransactionModel?) -> Unit) {
        transactionsRef.child(transactionId)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val transaction = snapshot.getValue(TransactionModel::class.java)
                    callback(true, transaction)
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    callback(false, null)
                }
            })
    }

    override fun updateTransaction(
        transactionId: String,
        updates: Map<String, Any>,
        callback: (Boolean, String?) -> Unit
    ) {
        transactionsRef.child(transactionId).updateChildren(updates)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { e -> callback(false, e.message) }
    }

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