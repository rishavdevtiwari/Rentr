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
}
