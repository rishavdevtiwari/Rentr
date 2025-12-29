package com.example.rentr.model

data class TransactionModel(
    val productId: String = "",
    val renterId: String = "",
    val sellerId: String = "",
    val basePrice: Double = 0.0,
    val rentalPrice: Double =0.0,
    val days:Int = 0,
    val penaltyDays:Int = 0,
    val paymentOption:String = "",
    val startTime:String = "",
    val endTime:String = "",
    val pickupLocation:String = ""
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "startTime" to startTime,
            "endTime" to endTime,
        )
    }
}
