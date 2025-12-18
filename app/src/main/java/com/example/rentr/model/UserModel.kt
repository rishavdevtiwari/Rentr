package com.example.rentr.model

data class UserModel(
    val fullName: String="",
    val gender : String = "",
    val phoneNumber : String = "",
    val dob: String = "",
    val email: String = "",
    val listings: List<String> = emptyList(),
    val verified: Boolean = false,
    val profileImage: String = ""
)
{
    fun toMap() : Map < String, Any?>{
        return mapOf(
            "fullName" to fullName,
            "gender" to gender,
            "phoneNumber" to phoneNumber,
            "dob" to dob,
            "listings" to listings,
            "verified" to verified,
            "profileImage" to profileImage,
        )
    }
}
