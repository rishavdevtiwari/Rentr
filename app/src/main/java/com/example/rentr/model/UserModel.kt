package com.example.rentr.model

data class UserModel(
    val fullName: String="",
    val gender : String = "",
    val phoneNumber : String = "",
    val uId : String = "",
    val dob: String = "",
    val email: String = "",
    val listings: List<String> = emptyList(),
    val verified: Boolean = false
)
{
    fun toMap() : Map < String, Any?>{
        return mapOf(
            "userId" to uId,
            "FullName" to fullName,
            "Gender" to gender,
            "PhoneNumber" to phoneNumber,
            "DOB" to dob,
            "email" to email,
            "listings" to listings,
            "verified" to verified
        )
    }
}
