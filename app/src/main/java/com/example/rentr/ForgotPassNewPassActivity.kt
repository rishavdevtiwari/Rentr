package com.example.rentr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.ui.theme.Button
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange

class ForgotPassNewPassActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForgotPassNewPassBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPassNewPassBody() {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }

    // Button is enabled only when both fields are not blank and they match
    val isEnabled = newPassword.isNotBlank() && confirmPassword.isNotBlank() && (newPassword == confirmPassword)

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val activity = LocalContext.current.let {
        if (it is Activity) it else null
    }

    Scaffold (
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    containerColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = null
                        )
                    }
                },
                title = {
                    Text(
                        text = "Forgot Password",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            verticalArrangement=Arrangement.SpaceEvenly,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(color = Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            Spacer(modifier = Modifier.height(25.dp))
            Row(horizontalArrangement=Arrangement.Center,
                modifier=Modifier.fillMaxWidth().size(300.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.securepassword),
                    contentDescription = null,
                    contentScale= ContentScale.Fit,
                    modifier = Modifier.size(300.dp)
                )
            }
            Spacer(modifier = Modifier.height(25.dp))
            Column() {
                Text(
                    text = "Set a new secure password.",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(25.dp))

                // New Password Field
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_lock_24),
                            contentDescription = "Password Icon",
                            tint = if (newPassword.isNotBlank()) Color.Black else Color.White
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { visibility = !visibility }) {
                            Icon(
                                painter = if (visibility) {
                                    painterResource(R.drawable.baseline_visibility_24)
                                } else {
                                    painterResource(R.drawable.baseline_visibility_off_24)
                                }, contentDescription = null,
                                tint = if (newPassword.isNotBlank()) Color.Black else Color.White
                            )
                        }
                    },
                    placeholder = {
                        Text(text = "New Password", color = Color.Gray)
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Field,
                        focusedIndicatorColor = Orange,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        focusedLeadingIconColor = Color.Black,
                        unfocusedLeadingIconColor = Color.White,
                        focusedTrailingIconColor = Color.Black,
                        unfocusedTrailingIconColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm New Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        // Using a placeholder icon; replace with your R.drawable.lock_icon or similar
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_lock_24),
                            contentDescription = "Confirm Password Icon",
                            tint = if (confirmPassword.isNotBlank()) Color.Black else Color.White
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { visibility = !visibility }) {
                            Icon(
                                painter = if (visibility) {
                                    painterResource(R.drawable.baseline_visibility_24)
                                } else {
                                    painterResource(R.drawable.baseline_visibility_off_24)
                                }, contentDescription = null,
                                tint = if (confirmPassword.isNotBlank()) Color.Black else Color.White
                            )
                        }
                    },
                    placeholder = {
                        Text(text = "Confirm New Password", color = Color.Gray)
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Field,
                        focusedIndicatorColor = Orange,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        focusedLeadingIconColor = Color.Black,
                        unfocusedLeadingIconColor = Color.White,
                        focusedTrailingIconColor = Color.Black,
                        unfocusedTrailingIconColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Reset Password Button
            Button(
                onClick = {
                    if (activity != null) {
                        // TODO: Implement password reset API call here
                        val intent = Intent(context, LoginActivity::class.java) // Redirect back to Login
                        context.startActivity(intent)
                        activity.finish()
                    }
                },
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnabled) Orange else Button,
                    disabledContainerColor = Button
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 15.dp
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(horizontal = 15.dp, vertical = 20.dp),
            ) {
                Text("Reset Password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPassNewPassPreview() {
    ForgotPassNewPassBody()
}