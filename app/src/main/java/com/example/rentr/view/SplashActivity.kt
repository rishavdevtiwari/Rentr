package com.example.rentr.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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

            val destination = if (rememberMe && currentUser != null) {
                DashboardActivity::class.java
            } else {
                LoginActivity::class.java
            }
            val intent = Intent(context, destination)
            context.startActivity(intent)
            activity.finish()
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
