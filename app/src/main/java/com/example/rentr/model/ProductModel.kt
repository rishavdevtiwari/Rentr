package com.example.rentr.model

data class ProductModel(
    val title: String = "",
    val listedBy: String = "",
    val description: String = "",
    val imageUrl: List<String> = emptyList(),
    val price: Double = 0.0,
    val productId: String = "", // primary key
    val availability: Boolean = true,
    val outOfStock: Boolean = false,
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val category: String = "", //foreign key,
    val verified: Boolean = false,

    val flagged: Boolean = false
){
    fun toMap() : Map < String, Any?> {
        return mapOf(
            "title" to title,
            "listedBy" to listedBy,
            "description" to description,
            "price" to price,
            "productId" to productId,
            "availability" to availability,
            "outOfStock" to outOfStock,
            "rating" to rating,
            "ratingCount" to ratingCount,
            "category" to category,
            "imageUrl" to imageUrl,
            "verified" to verified,
            "flagged" to flagged
        )
    }
}
