package com.example.rentr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rentr.model.UserModel
import com.example.rentr.repository.UserRepo
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class UserViewModel(val repo : UserRepo): ViewModel(){

    fun login(email: String, password: String,
              callback :(Boolean, String) -> Unit)
    {
        repo.login(email, password, callback)
    }
    fun register(email: String, password: String, callback: (Boolean, String, String) -> Unit){
        repo.register(email, password, callback)
    }
    fun forgetPassword(email: String,
                       callback: (Boolean, String) -> Unit)
    {
        repo.forgetPassword(email, callback)
    }
    fun updateProfile(userId: String, model: UserModel,
                      callback: (Boolean, String) -> Unit)
    {
        repo.updateProfile(userId, model, callback)
    }
    fun getCurrentUser() : FirebaseUser?{
        return repo.getCurrentUser()
    }
    fun logout(callback: (Boolean, String) -> Unit){
        repo.logout(callback)
    }
    
    fun changePassword(oldPass: String, newPass: String, callback: (Boolean, String) -> Unit) {
        repo.changePassword(oldPass, newPass, callback)
    }

    private val _user = MutableLiveData<UserModel?>()
    val user : LiveData<UserModel?>
        get() = _user
    private val _allUsers = MutableLiveData<List<UserModel>?>()
    val allUsers : MutableLiveData<List<UserModel>?>
        get() = _allUsers

    private val _loading = MutableLiveData<Boolean>()
    val loading : MutableLiveData<Boolean>
        get() = _loading

    fun getUserById(userId: String, callback:(Boolean,String, UserModel?) -> Unit){
        _loading.postValue(true)
        repo.getUserById(userId){ success, msg, data ->
            if(success){
                _loading.postValue(false)
                _user.postValue(data)
            }else{
                _loading.postValue(false)
                _user.postValue(null)
            }
            callback(success, msg, data)
        }
    }
    fun updateProfileImage(
        userId: String,
        imageUrl: String,
        callback: (Boolean, String?) -> Unit
    ) {
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("profileImage")
            .setValue(imageUrl)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener {
                callback(false, it.message)
            }
    }

    fun getAllUsers(callback:(Boolean, String, List<UserModel>) -> Unit){
        repo.getAllUsers (callback)
    }

    fun deleteAccount(userId: String, callback:(Boolean, String) -> Unit){
        repo.deleteAccount(userId,callback)
    }

    fun addUserToDatabase(
        userId: String,
        model:UserModel,
        callback:(Boolean, String) -> Unit
    ){
        repo.addUserToDatabase(userId,model,callback)
    }
}
