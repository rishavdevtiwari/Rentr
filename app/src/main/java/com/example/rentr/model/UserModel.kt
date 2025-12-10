package com.example.rentr.model

data class UserModel(
    val email: String,
    val firstName: String="",
    val lastName: String = "",
    val password: String = "",
    val gender : String = "",
    val phoneNumber : String = "",
    val uId : String = ""
)
{
    fun toMap() : Map < String, Any?>{
        return mapOf(
            "userId" to uId,
            "FirstName" to firstName,
            "LastName" to lastName,
            "Email" to email,
            "Gender" to gender,
            "PhoneNumber" to phoneNumber

        )
    }
}
