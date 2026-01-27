package com.example.rentr.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.R
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.BG40
import com.example.rentr.ui.theme.Button
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.viewmodel.UserViewModel

// --- CREDENTIAL MANAGER & GOOGLE IMPORTS ---
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

// --- FIREBASE IMPORTS ---
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginBody()
        }
    }
}

@Composable
fun LoginBody() {
    val userViewModelLogin = remember { UserViewModel(UserRepoImpl()) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val isEnabled = email.isNotBlank() && password.isNotBlank()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    // Helper function for Google Sign In
    fun onGoogleSignInClicked() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("603256134770-utm1u6ee3jnohkna3vo7peii3k6ejuhh.apps.googleusercontent.com")
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    userViewModelLogin.signInWithGoogle(credential.idToken) { success, msg ->
                        if (success) {
                            Toast.makeText(context, "Google Login Successful", Toast.LENGTH_SHORT).show()
                            context.startActivity(Intent(context, DashboardActivity::class.java))
                            activity?.finish()
                        } else {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: GetCredentialException) {
                Log.e("Auth", "Google Login Error: ${e.message}")
                Toast.makeText(context, "Google Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(color = Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Image(painter = painterResource(id = R.drawable.rent), contentDescription = null)
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Login to your\nAccount",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                lineHeight = 45.sp
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_email_24), contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).testTag("email"),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Field,
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).testTag("password"),
                trailingIcon = {
                    IconButton(onClick = { visibility = !visibility }) {
                        Icon(
                            painter = if (visibility) painterResource(R.drawable.baseline_visibility_24)
                            else painterResource(R.drawable.baseline_visibility_off_24),
                            contentDescription = null
                        )
                    }
                },
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Field,
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White
                )
            )

            // Remember Me Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = BG40, checkmarkColor = Orange)
                )
                Text("Remember Me", color = Color.White, fontSize = 15.sp)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { context.startActivity(Intent(context, ForgotPassGmailActivity::class.java)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Forgot password?", color = Color.Gray)
                }
            }

            // Manual Login Button
            Button(
                onClick = {
                    userViewModelLogin.login(email, password) { success, msg ->
                        if (success) {
                            val sharedPreferences = context.getSharedPreferences("rentr_prefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit().putBoolean("remember_me", rememberMe).apply()

                            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                    val database = FirebaseDatabase.getInstance("https://rentr-db9e6-default-rtdb.firebaseio.com/")
                                    database.getReference("users").child(userId).child("fcmToken").setValue(token)
                                }
                            }

                            Toast.makeText(context, "Logged In.", Toast.LENGTH_SHORT).show()
                            context.startActivity(Intent(context, DashboardActivity::class.java))
                            activity?.finish()
                        } else {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = Orange, disabledContainerColor = Button),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier.fillMaxWidth().height(90.dp).padding(horizontal = 15.dp, vertical = 15.dp).testTag("login"),
            ) {
                Text("Log In", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // --- GOOGLE LOGIN BUTTON ---
            OutlinedButton(
                onClick = { onGoogleSignInClicked() },
                modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 15.dp).testTag("google_login"),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                shape = RoundedCornerShape(25.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.Medium)
                }
            }

//            TextButton(
//                onClick = { context.startActivity(Intent(context, ForgotPassGmailActivity::class.java)) },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Forgot password?", color = Color.Gray)
//            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Don't have an account? ", color = Color.Gray)
                Text(
                    text = "Sign up",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, RegistrationActivity::class.java))
                    }.testTag("register")
                )
            }
        }
    }
}