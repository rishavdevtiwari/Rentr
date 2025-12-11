package com.example.rentr.repository

import com.example.rentr.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepoImp1 : UserRepo {
    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref : DatabaseReference = database.getReference("users")

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful) {
                callback(true, "Login Successful")
            }else{
                    callback(false, "Login Failed")

                }
            }
    }

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful){
                callback(true, "Registration complete. You can now sign it through login page", "${auth.currentUser?.uid}")

            }else{
                callback(false, "Registration failed", "")
            }
        }

    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if(it.isSuccessful){
                callback(true, "Sent reset code check your email")
            }else{
                callback(false, "Failed to send reset code")

            }
        }
    }

    override fun updateProfile(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).updateChildren(model.toMap()).addOnCompleteListener {
            if(it.isSuccessful){
                callback(true, "Profile updated")
            }else{
                callback(false, "Failed to update profile")
            }
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        ref.child(userId)
            . addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val users = snapshot.getValue(UserModel :: class.java)
                        if(users != null){
                            callback(true, "profile fetched", users)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false,error.message,null)
                }
            })
    }

    override fun getAllUsers(callback: (Boolean, String, List<UserModel>) -> Unit) {
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val allUsers = mutableListOf<UserModel>()
                    for (data in snapshot.children) {
                        val user = data.getValue(UserModel::class.java)
                        if (user != null) {
                            allUsers.add(user)
                        }
                    }
                    callback(true, "User fetched", allUsers)
                } else {
                    callback(true, "No users found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).removeValue().addOnCompleteListener{ 
            if(it.isSuccessful){
                callback(true, "Account deleted")
            }
            else{
                callback(false, "Failed to delete account")
            }
        }
    }

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model).addOnCompleteListener{
            if(it.isSuccessful){
                callback(true, "User added to database")
            }else{
                callback(false, "Failed to add user to database")
            }
        }
    }

}