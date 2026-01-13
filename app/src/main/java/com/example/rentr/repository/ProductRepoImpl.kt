package com.example.rentr.repository

import com.example.rentr.model.ProductModel
import com.google.firebase.database.*

class ProductRepoImpl : ProductRepo {

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("products")

    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_APPROVED = "approved"
        const val STATUS_PAID = "paid"
        const val STATUS_RENTED = "rented"
        const val STATUS_RETURNING = "returning"
        const val STATUS_RETURNED = "returned"
        const val STATUS_CANCELLED = "cancelled"
        const val STATUS_COMPLETED = "completed"
    }

    override fun addProduct(
        product: ProductModel,
        callback: (Boolean, String, String?) -> Unit
    ) {
        val newRef = ref.push()
        val productId = newRef.key

        if (productId == null) {
            callback(false, "Failed to create a new product ID.", null)
            return
        }

        newRef.setValue(product.copy(productId = productId)).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Product added successfully", productId)
            } else {
                callback(false, task.exception?.message ?: "Unknown error while adding product", null)
            }
        }
    }

    override fun updateProduct(
        productId: String,
        product: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productId).updateChildren(product.toMap()).addOnCompleteListener { 
            if (it.isSuccessful) {
                callback(true, "Product updated")
            } else {
                callback(false, "Failed to update product")
            }
        }
    }

    override fun deleteProduct(
        productId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product deleted")
            } else {
                callback(false, "Failed to delete product")
            }
        }
    }

    override fun getProductById(
        productId: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    ) {
        ref.child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val product = snapshot.getValue(ProductModel::class.java)
                if (product != null) {
                    callback(true, "Product fetched",  product.copy(productId = snapshot.key ?: productId))
                } else {
                    callback(false, "Product not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllProducts(callback: (Boolean, String, List<ProductModel>) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<ProductModel>()
                for (data in snapshot.children) {
                    data.getValue(ProductModel::class.java)?.let { product ->
                        products.add(product.copy(productId = data.key ?: "")) }
                }
                callback(true, "Products fetched", products)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getAllProductsByCategory(
        category: String,
        callback: (Boolean, String, List<ProductModel>?) -> Unit
    ) {
        ref.orderByChild("category").equalTo(category)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = mutableListOf<ProductModel>()
                    for (data in snapshot.children) {
                        data.getValue(ProductModel::class.java)?.let { product ->
                            products.add(product.copy(productId = data.key ?: "")) }
                    }
                    callback(true, "Products fetched", products)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun getAvailableProducts(callback: (Boolean, String, List<ProductModel>) -> Unit) {
        ref.orderByChild("availability").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = mutableListOf<ProductModel>()
                    for (data in snapshot.children) {
                        data.getValue(ProductModel::class.java)?.let { product ->
                            // Copy productId from document key
                            products.add(product.copy(productId = data.key ?: ""))}
                    }
                    callback(true, "Available products fetched", products)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun getAllProductsByUser(
        userId: String,
        callback: (Boolean, String, List<ProductModel>) -> Unit
    ) {
        ref.orderByChild("listedBy").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = mutableListOf<ProductModel>()
                    for (data in snapshot.children) {
                        data.getValue(ProductModel::class.java)?.let {  product ->
                            products.add(product.copy(productId = data.key ?: "")) }
                    }
                    callback(true, "User's products fetched", products)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun updateAvailability(
        productId: String,
        available: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productId).child("availability").setValue(available).addOnCompleteListener { 
            if (it.isSuccessful) {
                callback(true, "Availability updated")
            } else {
                callback(false, "Failed to update availability")
            }
        }
    }

    override fun updateRating(productId: String, userId: String, rating: Int, callback: (Boolean, String) -> Unit) {
        ref.child(productId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val product = currentData.getValue(ProductModel::class.java)
                if (product == null) {
                    return Transaction.success(currentData)
                }

                val updatedRatedBy = product.ratedBy.toMutableMap()
                val alreadyRated = updatedRatedBy.containsKey(userId)

                if (rating > 0) {
                    // Add or update rating
                    updatedRatedBy[userId] = rating
                } else {
                    // Remove rating
                    updatedRatedBy.remove(userId)
                }

                val newRatingCount = updatedRatedBy.size
                val newTotalRating = updatedRatedBy.values.sum()
                val newAverageRating = if (newRatingCount > 0) {
                    newTotalRating.toDouble() / newRatingCount
                } else {
                    0.0
                }

                val updatedProduct = product.copy(
                    ratedBy = updatedRatedBy,
                    ratingCount = newRatingCount,
                    rating = newAverageRating
                )

                currentData.value = updatedProduct
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    callback(false, error.message)
                } else if (!committed) {
                    callback(false, "Rating update was not committed.")
                } else {
                    callback(true, "Rating updated successfully.")
                }
            }
        })
    }

    override fun getFlaggedProducts(callback: (Boolean, String, List<ProductModel>) -> Unit) {
        ref.orderByChild("flagged").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = mutableListOf<ProductModel>()
                    for (data in snapshot.children) {
                        data.getValue(ProductModel::class.java)?.let { product ->
                            val productWithId = product.copy(productId = data.key ?: "")
                            if (productWithId.flagged) {
                                products.add(productWithId)
                            }
                        }
                    }
                    callback(true, "Flagged products fetched", products)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun updateProductFlags(productId: String, product: ProductModel, callback: (Boolean, String) -> Unit) {
        ref.child(productId).updateChildren(product.toMap()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Product flags updated")
            } else {
                callback(false, task.exception?.message ?: "Failed to update product flags")
            }
        }
    }

    override fun flagProduct(
        productId: String,
        userId: String,
        reason: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val product = currentData.getValue(ProductModel::class.java)
                if (product == null) {
                    return Transaction.success(currentData)
                }

                // Check if user already flagged
                if (product.flaggedBy.contains(userId)) {
                    return Transaction.success(currentData) // User already flagged
                }

                // Add user to flaggedBy list
                val updatedFlaggedBy = product.flaggedBy.toMutableList().apply {
                    add(userId)
                }

                // Add reason to flaggedReason list (avoid duplicates)
                val updatedFlaggedReason = product.flaggedReason.toMutableList().apply {
                    if (!contains(reason)) {
                        add(reason)
                    }
                }

                // Update product
                val updatedProduct = product.copy(
                    flaggedBy = updatedFlaggedBy,
                    flaggedReason = updatedFlaggedReason,
//                    flagged = true
                )

                currentData.value = updatedProduct
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    callback(false, error.message)
                } else if (committed) {
                    // Check if user was actually added
                    val product = currentData?.getValue(ProductModel::class.java)
                    if (product?.flaggedBy?.contains(userId) == true) {
                        callback(true, "Product flagged successfully")
                    } else {
                        callback(false, "User already flagged this product or transaction failed")
                    }
                } else {
                    callback(false, "Transaction not committed")
                }
            }
        })
    }

    override fun endRental(productId: String, callback: (Boolean, String) -> Unit) {
        val updates = mapOf(
            "rentalStatus" to "",
            "rentalRequesterId" to "",
            "rentalDays" to 1,
            "rentalStartDate" to 0L,
            "outOfStock" to false,
            "availability" to true
        )

        ref.child(productId).updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Rental ended and product is now available.")
            } else {
                callback(false, task.exception?.message ?: "Failed to end rental")
            }
        }
    }



    override fun placeRentalRequest(
        productId: String,
        renterId: String,
        days: Int,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val product = currentData.getValue(ProductModel::class.java) ?:
                return Transaction.success(currentData)

                // Validate product is available for rental
                if (!product.availability ||
                    product.outOfStock ||
                    product.flagged ||
                    product.rentalStatus.isNotEmpty()) {
                    return Transaction.abort()
                }

                // Set rental request - Use explicit status
                val updatedProduct = product.copy(
                    rentalRequesterId = renterId,
                    rentalDays = days,
                    rentalStatus = "pending",
                    rentalStartDate = 0L,
                    rentalEndDate = 0L,
                    availability = false
                )

                currentData.value = updatedProduct
                return Transaction.success(currentData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> callback(false, error.message)
                    !committed -> callback(false, "Product unavailable or already requested")
                    else -> callback(true, "Rental request placed")
                }
            }
        })
    }

    override fun cancelRentalRequest(
        productId: String,
        renterId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val product = currentData.getValue(ProductModel::class.java) ?: return Transaction.success(currentData)

                // Check if this user placed the request
                if (product.rentalRequesterId != renterId) {
                    return Transaction.abort()
                }

                // Check if request can be cancelled (only if pending)
                if (product.rentalStatus != "pending") {
                    return Transaction.abort()
                }

                val updatedProduct = product.copy(
                    rentalRequesterId = "",
                    rentalDays = 1,
                    rentalStatus = "",
                    rentalStartDate = 0L,
                    rentalEndDate = 0L
                )

                currentData.value = updatedProduct
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> callback(false, error.message)
                    !committed -> callback(false, "Cannot cancel request")
                    else -> callback(true, "Rental request cancelled")
                }
            }
        })
    }
    override fun approveRentalRequest(productId: String, callback: (Boolean, String) -> Unit) {
        ref.child(productId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val product = currentData.getValue(ProductModel::class.java) ?:
                return Transaction.success(currentData)

                // Check if product has a pending request
                if (product.rentalStatus != "pending") {
                    return Transaction.abort()
                }

                val updatedProduct = product.copy(
                    rentalStatus = "approved"
                )

                currentData.value = updatedProduct
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> callback(false, error.message)
                    !committed -> callback(false, "Cannot approve request")
                    else -> callback(true, "Rental request approved")
                }
            }
        })
    }

    override fun rejectRentalRequest(productId: String, callback: (Boolean, String) -> Unit) {
        ref.child(productId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val product = currentData.getValue(ProductModel::class.java) ?:
                return Transaction.success(currentData)

                // Check if product has a pending request
                if (product.rentalStatus != "pending") {
                    return Transaction.abort()
                }

                val updatedProduct = product.copy(
                    rentalRequesterId = "",
                    rentalDays = 1,
                    rentalStatus = "",
                    rentalStartDate = 0L,
                    rentalEndDate = 0L
                )

                currentData.value = updatedProduct
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> callback(false, error.message)
                    !committed -> callback(false, "Cannot reject request")
                    else -> callback(true, "Rental request rejected")
                }
            }
        })
    }

    override fun handoverProduct(
        productId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val product = currentData.getValue(ProductModel::class.java)
                    ?: return Transaction.success(currentData)


                if (product.rentalStatus != STATUS_PAID) {
                    return Transaction.abort()
                }

                val currentTime = System.currentTimeMillis()
                val endTime = currentTime + (product.rentalDays * 24 * 60 * 60 * 1000L)

                val updatedProduct = product.copy(
                    rentalStatus = STATUS_RENTED,
                    rentalStartDate = currentTime,
                    rentalEndDate = endTime,
                    outOfStock = true,
                    availability = false
                )

                currentData.value = updatedProduct
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> callback(false, error.message)
                    !committed -> callback(false, "Product must be in 'paid' status for handover")
                    else -> callback(true, "Product handed over successfully")
                }
            }
        })
    }
    override fun completeCashPayment(productId: String, callback: (Boolean, String) -> Unit) {
        ref.child(productId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val product = currentData.getValue(ProductModel::class.java)
                    ?: return Transaction.success(currentData)

                // Only APPROVED Cash on Delivery can be marked as paid
                if (product.rentalStatus != STATUS_APPROVED || product.paymentMethod != "Cash on Delivery") {
                    return Transaction.abort()
                }

                val updatedProduct = product.copy(
                    rentalStatus = STATUS_PAID,
                    availability = false,
                    outOfStock = true
                )

                currentData.value = updatedProduct
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> callback(false, error.message)
                    !committed -> callback(false, "Product not ready for cash payment completion")
                    else -> callback(true, "Cash payment completed")
                }
            }
        })
    }
    override fun requestReturn(
        productId: String,
        renterId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val product = currentData.getValue(ProductModel::class.java) ?: return Transaction.success(currentData)

                // Check if product is currently rented by this user
                if (product.rentalRequesterId != renterId || product.rentalStatus != "rented") {
                    return Transaction.abort()
                }

                // Check if rental period has ended or is about to end
                val currentTime = System.currentTimeMillis()
                val isWithinRentalPeriod = currentTime <= product.rentalEndDate

                if (!isWithinRentalPeriod) {
                    // Auto-return if rental period ended
                    val updatedProduct = product.copy(
                        rentalStatus = "returning",
                        rentalEndDate = currentTime // Update end time to actual return time
                    )
                    currentData.value = updatedProduct
                } else {
                    // Manual return request
                    val updatedProduct = product.copy(
                        rentalStatus = "returning"
                    )
                    currentData.value = updatedProduct
                }

                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> callback(false, error.message)
                    !committed -> callback(false, "Cannot request return")
                    else -> callback(true, "Return requested")
                }
            }
        })
    }

    override fun verifyReturn(
        productId: String,
        callback: (Boolean, String, Long) -> Unit
    ) {
        ref.child(productId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val product = currentData.getValue(ProductModel::class.java) ?: return Transaction.success(currentData)

                // Check if return was requested
                if (product.rentalStatus != "returning") {
                    return Transaction.abort()
                }

                val returnTime = System.currentTimeMillis()
                val actualRentalDays = ((returnTime - product.rentalStartDate) / (24 * 60 * 60 * 1000)).toInt()

                val updatedProduct = product.copy(
                    rentalStatus = "",
                    rentalRequesterId = "",
                    rentalDays = 1,
                    rentalStartDate = 0L,
                    rentalEndDate = returnTime,
                    outOfStock = false,
                    availability = true
                )

                currentData.value = updatedProduct
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> callback(false, error.message, 0L)
                    !committed -> callback(false, "Cannot verify return", 0L)
                    else -> {
                        val product = currentData?.getValue(ProductModel::class.java)
                        val returnTime = System.currentTimeMillis()
                        callback(true, "Return verified", returnTime)
                    }
                }
            }
        })
    }

    override fun markProductForReview(productId: String, callback: (Boolean, String) -> Unit) {
        val productRef = ref.child(productId)

        productRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val product = currentData.getValue(ProductModel::class.java)
                if (product == null) {
                    return Transaction.success(currentData)
                }

                // Only mark for review if there are flags
                if (product.flaggedBy.isEmpty()) {
                    return Transaction.abort()
                }

                // Set flagged = true and hide from listings
                val updatedProduct = product.copy(
                    flagged = true,
                    availability = false
                )

                currentData.value = updatedProduct
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    callback(false, error.message)
                } else if (!committed) {
                    callback(false, "No flags to review or product not found")
                } else {
                    callback(true, "Product marked for review")
                }
            }
        })
    }

    override fun updateRentalStatus(
        productId: String,
        status: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productId).child("rentalStatus").setValue(status)
            .addOnSuccessListener {
                callback(true, "Rental status updated to $status")
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to update rental status")
            }
    }

    override fun clearFlags(productId: String, callback: (Boolean, String) -> Unit) {
        ref.child(productId).child("flagged").setValue(false)
            .addOnCompleteListener { flagTask ->
                if (flagTask.isSuccessful) {
                    ref.child(productId).child("flaggedBy").setValue(emptyList<String>())
                        .addOnCompleteListener { listTask ->
                            if (listTask.isSuccessful) {
                                ref.child(productId).child("flaggedReason").setValue(emptyList<String>())
                                    .addOnCompleteListener { reasonTask ->
                                        if (reasonTask.isSuccessful) {
                                            ref.child(productId).child("appealReason").setValue("")
                                                .addOnCompleteListener { appealTask ->
                                                    if (appealTask.isSuccessful) {
                                                        callback(true, "All flags cleared")
                                                    } else {
                                                        callback(false, "Failed to clear appeal reason")
                                                    }
                                                }
                                        } else {
                                            callback(false, "Failed to clear flag reasons")
                                        }
                                    }
                            } else {
                                callback(false, "Failed to clear flaggedBy list")
                            }
                        }
                } else {
                    callback(false, "Failed to update flagged status")
                }
            }
    }
}

