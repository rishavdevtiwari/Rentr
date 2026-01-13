package com.example.rentr.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log // Added for logging
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.rentr.R
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.viewmodel.UserViewModel
// --- FIREBASE IMPORTS ---
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SplashScreen()
        }
    }

}

@Composable
fun SplashScreen() {
    val context = LocalContext.current
    val activity = context as Activity
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_loop))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1
    )

    LaunchedEffect(progress) {
        if (progress == 1f) {
            val sharedPreferences = context.getSharedPreferences("rentr_prefs", Context.MODE_PRIVATE)
            val rememberMe = sharedPreferences.getBoolean("remember_me", false)
            val currentUser = userViewModel.getCurrentUser()

            if (rememberMe && currentUser != null) {
                // 1. User is logged in: FETCH & SAVE TOKEN
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d("FCM_DEBUG", "Token Fetched: $token")

                        // --- FIX: Using Hardcoded Database URL ---
                        val database = FirebaseDatabase.getInstance("https://rentr-db9e6-default-rtdb.firebaseio.com/")

                        database.getReference("users")
                            .child(currentUser.uid)
                            .child("fcmToken")
                            .setValue(token)
                            .addOnSuccessListener {
                                Log.d("FCM_DEBUG", "✅ Token saved to DB successfully!")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FCM_DEBUG", "❌ Failed to save to DB", e)
                            }
                    } else {
                        Log.e("FCM_DEBUG", "❌ Failed to fetch token from Google", task.exception)
                    }

                    // 2. Navigate regardless of success/failure
                    val intent = Intent(context, DashboardActivity::class.java)
                    context.startActivity(intent)
                    activity.finish()
                }
            } else {
                // User NOT logged in -> Go to Login
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
                activity.finish()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(color = Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress }
        )
    }
}