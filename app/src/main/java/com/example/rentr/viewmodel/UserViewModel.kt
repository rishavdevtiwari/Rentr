package com.example.rentr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentr.model.KYCStatus
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


    fun verifyUserKYC(
        userId: String,
        approved: Boolean,
        reason: String = "",
        callback: (Boolean, String?) -> Unit
    ) {
        repo.verifyUserKYC(userId, approved, reason, callback)
    }

    fun getKYCStatus(
        userId: String,
        callback: (Boolean, String, Map<String, KYCStatus>?) -> Unit
    ) {
        repo.getKYCStatus(userId, callback)
    }

    fun updateKYCStatus(
        userId: String,
        documentType: String,
        status: String,
        callback: (Boolean, String?) -> Unit
    ) {
        repo.updateKYCStatus(userId, documentType, status, callback)
    }


//    fun getAllUsers(callback:(Boolean, String, List<UserModel>) -> Unit){
//        repo.getAllUsers (callback)
//    }

    fun getAllUsers(callback:(Boolean, String, List<UserModel>) -> Unit){
        _loading.postValue(true)
        repo.getAllUsers { success, msg, users ->
            if(success) {
                _allUsers.postValue(users)
            } else {
                _allUsers.postValue(emptyList())
            }
            _loading.postValue(false)
            callback(success, msg, users)
        }
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

    class Factory(private val repo: UserRepo) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserViewModel(repo) as T
        }
    }
}
