package com.example.rentr.repository

import com.example.rentr.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepo {

//   {
//        "Success" : true
//        "message " : "Login success"
//    }
    fun login(email: String, password: String,
              callback :(Boolean, String) -> Unit)
    fun register(email: String, password: String, callback: (Boolean, String, String) -> Unit)
    fun forgetPassword(email: String,
                       callback: (Boolean, String) -> Unit)
    fun updateProfile(userId: String, model: UserModel,
                      callback: (Boolean, String) -> Unit)
    fun changePassword(oldPass: String, newPass: String, callback: (Boolean, String) -> Unit)

    fun updateProfileImage(userId: String, imageUrl: String,callback: (Boolean, String?) -> Unit)

    fun updateKyc(userId: String, kycUrl: String, callback: (Boolean, String?) -> Unit)
    fun removeKyc(userId: String, callback: (Boolean, String?) -> Unit)

    fun getCurrentUser() : FirebaseUser?
    fun logout(callback: (Boolean, String) -> Unit)
    fun getUserById(userId: String, callback:(Boolean,String, UserModel?) -> Unit)
    fun getAllUsers(callback:(Boolean, String, List<UserModel>) -> Unit)
    fun deleteAccount(userId: String, callback:(Boolean, String) -> Unit)

    fun addUserToDatabase(
        userId: String,
        model:UserModel,
        callback:(Boolean, String) -> Unit
    )

    fun verifyUserKYC(
        userId: String,
        approved: Boolean,
        callback: (Boolean, String?) -> Unit
    )


}

//    interface ProductRepo {
//        fun addProduct()
//        fun updateProduct()
//        fun deleteProduct()
//        fun getProductById()
//        fun getAllProducts()
//    }