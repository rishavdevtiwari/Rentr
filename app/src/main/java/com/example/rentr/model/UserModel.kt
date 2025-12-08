package com.example.rentr.model

data class UserModel(
    val email: String,
    val firstName: String="",
    val lastName: String = "",
    val password: String = "",
    val  gender : String = "",
    val phoneNumber : String = ""
)
