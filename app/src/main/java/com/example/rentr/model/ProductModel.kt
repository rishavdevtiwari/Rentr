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
    val ratedBy : Map <String,Int> = emptyMap(),
    val category: String = "", //foreign key
    val verified: Boolean = false,
    val flaggedBy: List<String> = emptyList(), // this will resolve the flagCount and the button greyed logic
    val flagged: Boolean = false,
    val flaggedReason:List<String> = emptyList(),
    val appealReason:String="",
    val rentalRequesterId: String = "",
    val rentalStatus: String = "",
    val rentalDays: Int = 1,
    val rentalStartDate: Long = 0L
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
            "ratedBy" to ratedBy,
            "imageUrl" to imageUrl,
            "verified" to verified,
            "flaggedBy" to flaggedBy,
            "flagged" to flagged,
            "flaggedReason" to flaggedReason,
            "appealReason" to appealReason,
            "rentalRequesterId" to rentalRequesterId,
            "rentalStatus" to rentalStatus,
            "rentalDays" to rentalDays,
            "rentalStartDate" to rentalStartDate
        )
    }
}
