package com.example.rentr.repository

import com.example.rentr.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class UserRepoImpl : UserRepo {

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
        ref.child(userId).updateChildren(model.toMap())
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

//    override fun getAllUsers(callback: (Boolean, String, List<UserModel>) -> Unit) {
//        ref.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val users = mutableListOf<UserModel>()
//                for (data in snapshot.children) {
//                    data.getValue(UserModel::class.java)?.let { users.add(it) }
//                }
//                callback(true, "Users fetched", users)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                callback(false, error.message, emptyList())
//            }
//        })
//    }

    override fun getAllUsers(callback: (Boolean, String, Map<String, UserModel>) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersMap = mutableMapOf<String, UserModel>()
                for (data in snapshot.children) {
                    val user = data.getValue(UserModel::class.java)
                    if (user != null) {
                        usersMap[data.key ?: ""] = user
                    }
                }
                callback(true, "Users fetched", usersMap)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyMap())
            }
        })
    }

    override fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "No authenticated user")
            return
        }

        val productsRef = database.getReference("products")
        val usersRef = database.getReference("users")

        //  Delete all listings by this user
        productsRef
            .orderByChild("listedBy")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val deleteTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()

                    for (product in snapshot.children) {
                        deleteTasks.add(product.ref.removeValue())
                    }

                    // Wait for all listings to delete
                    com.google.android.gms.tasks.Tasks
                        .whenAllComplete(deleteTasks)
                        .addOnCompleteListener {

                            // Delete user data from DB
                            usersRef.child(userId).removeValue()
                                .addOnCompleteListener { userDbTask ->

                                    if (!userDbTask.isSuccessful) {
                                        callback(false, "Failed to delete user data")
                                        return@addOnCompleteListener
                                    }

                                    // Delete Firebase Auth account
                                    currentUser.delete()
                                        .addOnCompleteListener { authTask ->
                                            if (authTask.isSuccessful) {
                                                callback(true, "User and all listings deleted successfully")
                                            } else {
                                                callback(
                                                    false,
                                                    authTask.exception?.message
                                                        ?: "Failed to delete auth user (reauth required)"
                                                )
                                            }
                                        }
                                }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
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

       override fun verifyUserKYC(
        userId: String,
        approved: Boolean,
        callback: (Boolean, String?) -> Unit
    ) {
        ref.child(userId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val user = currentData.getValue(UserModel::class.java)
                if (user == null) {
                    return Transaction.success(currentData)
                }

                if (approved) {
                    // Approve KYC - mark as verified
                    currentData.child("verified").value = true
                } else {
                    // Reject KYC - mark as not verified and clear KYC URLs
                    currentData.child("verified").value = false
                    currentData.child("kycUrl").value = emptyList<String>()
                }

                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error == null && committed) {
                    callback(true, null)
                } else {
                    callback(false, error?.message ?: "Transaction failed")
                }
            }
        })
    }

    override fun incrementFlagCount(userId: String, callback: (Boolean, String) -> Unit) {
        ref.child(userId).child("flagCount").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentCount = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentCount + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error == null && committed) {
                    callback(true, "Flag count incremented")
                } else {
                    callback(false, error?.message ?: "Failed to increment flag count")
                }
            }
        })
    }

    override fun decrementFlagCount(userId: String, callback: (Boolean, String) -> Unit) {
        ref.child(userId).child("flagCount").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentCount = currentData.getValue(Int::class.java) ?: 0
                val newCount = if (currentCount > 0) currentCount - 1 else 0
                currentData.value = newCount
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error == null && committed) {
                    callback(true, "Flag count decremented")
                } else {
                    callback(false, error?.message ?: "Failed to decrement flag count")
                }
            }
        })
    }

    override fun changePassword(
        oldPass: String,
        newPass: String,
        callback: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser
        if (user?.email == null) {
            callback(false, "No user is currently signed in.")
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, oldPass)
        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        callback(true, "Password updated successfully.")
                    } else {
                        callback(false, updateTask.exception?.message ?: "Could not update password.")
                    }
                }
            } else {
                callback(false, reauthTask.exception?.message ?: "Re-authentication failed.")
            }
        }
    }

    override fun updateProfileImage(
        userId: String,
        imageUrl: String,
        callback: (Boolean, String?) -> Unit
    ) {
        ref.child(userId).child("profileImage").setValue(imageUrl){
                error, _ ->
            if(error == null){
                callback(true, null)
            }else{
                callback(false, error.message)
            }
        }
    }

    override fun updateKyc(
        userId: String,
        kycUrl: String,
        callback: (Boolean, String?) -> Unit
    ) {
        ref.child(userId).child("kycUrl").setValue(kycUrl){
                error, _ ->
            if(error == null){
                callback(true, null)
            }else{
                callback(false, error.message)
            }
        }
    }

    override fun removeKyc(
        userId: String,
        callback: (Boolean, String?) -> Unit
    ) {
        ref.child(userId).child("kycUrl").removeValue { error, _ ->
            if (error == null) {
                callback(true, null)
            } else {
                callback(false, error.message)
            }
        }
    }

    override fun signInWithGoogle(idToken: String, callback: (Boolean, String) -> Unit) {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Check if user is new; if so, you might want to call addUserToDatabase() here 
                    callback(true, "Login Successful")
                } else {
                    callback(false, task.exception?.message ?: "Authentication Failed")
                }
            }
    }
}
