package com.example.rentr.model

data class UserModel(
    val userId: String = "",
    val fullName: String="",
    val gender : String = "",
    val phoneNumber : String = "",
    val dob: String = "",
    val email: String = "",
    val listings: List<String> = emptyList(),
    val verified: Boolean = false,
    val profileImage: String = "",
    val kycUrl: List<String> = emptyList(),
    var kycDetails: Map<String, KYCStatus> = emptyMap(),
    var kycRejectionReason: String = "",
    var kycSubmittedAt: Long = 0
)
{
    fun toMap() : Map < String, Any?>{
        return mapOf(
            "userId" to userId,
            "fullName" to fullName,
            "gender" to gender,
            "phoneNumber" to phoneNumber,
            "dob" to dob,
            "listings" to listings,
            "verified" to verified,
            "profileImage" to profileImage,
            "kycUrl" to kycUrl
        )
    }
}
data class KYCStatus(
    val documentUrl: String = "",
    val documentType: String = "", // "citizenship_front", "citizenship_back", "pan", "bank", "profile"
    val status: String = "pending", // "pending", "approved", "rejected"
    val uploadedAt: Long = 0
)
