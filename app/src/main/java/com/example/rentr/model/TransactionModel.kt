package com.example.rentr.model

data class TransactionModel(
    val transactionId: String = "",
    val productId: String = "",
    val renterId: String = "",
    val sellerId: String = "",
    val basePrice: Double = 0.0,
    val rentalPrice: Double = 0.0,
    val days: Int = 0,
    val penaltyDays: Int = 0,
    val paymentOption: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val pickupLocation: String = "",
    val paymentId: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "transactionId" to transactionId,
            "productId" to productId,
            "renterId" to renterId,
            "sellerId" to sellerId,
            "basePrice" to basePrice,
            "rentalPrice" to rentalPrice,
            "days" to days,
            "penaltyDays" to penaltyDays,
            "paymentOption" to paymentOption,
            "startTime" to startTime,
            "endTime" to endTime,
            "pickupLocation" to pickupLocation,
            "paymentId" to paymentId
        )
    }
}