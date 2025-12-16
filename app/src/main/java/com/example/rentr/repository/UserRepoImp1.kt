package com.example.rentr.repository

import com.example.rentr.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepoImp1 : UserRepo {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref = database.getReference("users")
    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: ""
                    callback(true, "User created successfully", userId)
                } else {
                    callback(false, task.exception?.message ?: "Unknown error", "")
                }
            }
    }

    override fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Login successful")
                } else {
                    callback(false, task.exception?.message ?: "Unknown error")
                }
            }
    }

    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Password reset email sent")
                } else {
                    callback(false, task.exception?.message ?: "Unknown error")
                }
            }
    }

    override fun updateProfile(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Profile updated")
                } else {
                    callback(false, task.exception?.message ?: "Unknown error")
                }
            }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        auth.signOut()
        callback(true, "Logged out")
    }

    override fun getUserById(userId: String, callback: (Boolean, String, UserModel?) -> Unit) {
        ref.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                if (user != null) {
                    callback(true, "User found", user)
                } else {
                    callback(false, "User not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllUsers(callback: (Boolean, String, List<UserModel>) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<UserModel>()
                for (data in snapshot.children) {
                    data.getValue(UserModel::class.java)?.let { users.add(it) }
                }
                callback(true, "Users fetched", users)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit) {
        ref.child(userId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Account deleted")
            } else {
                callback(false, task.exception?.message ?: "Unknown error")
            }
        }
    }

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "User added to database")
            } else {
                callback(false, task.exception?.message ?: "Unknown error")
            }
        }
    }
}
