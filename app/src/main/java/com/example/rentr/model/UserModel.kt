package com.example.rentr.model

import com.google.firebase.database.Exclude

data class UserModel(
    val userId: String = "",
    val fullName: String = "",
    val gender: String = "",
    val phoneNumber: String = "",
    val dob: String = "",
    val email: String = "",
    val listings: List<String> = emptyList(),
    val verified: Boolean = false,
    val profileImage: String = "",
    val kycUrl: List<String> = emptyList()
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "fullName" to fullName,
            "gender" to gender,
            "phoneNumber" to phoneNumber,
            "dob" to dob,
            "email" to email,
            "listings" to listings,
            "verified" to verified,
            "profileImage" to profileImage,
            "kycUrl" to kycUrl
        )
    }
}

//setValue() or updateChildren() save and update everything
//using @Exclude we can choose what to exclude in tomap