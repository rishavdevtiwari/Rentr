package com.example.rentr.repository

import ProductRepo
import com.example.rentr.model.ProductModel
import com.google.firebase.database.*

class ProductRepoImpl : ProductRepo {

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("products")

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
                    callback(true, "Product fetched", product)
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
                    data.getValue(ProductModel::class.java)?.let { products.add(it) }
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
                        data.getValue(ProductModel::class.java)?.let { products.add(it) }
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
                        data.getValue(ProductModel::class.java)?.let { products.add(it) }
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
                        data.getValue(ProductModel::class.java)?.let { products.add(it) }
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
                            if (product.flagged) {
                                products.add(product)
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
                    flagged = true
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

