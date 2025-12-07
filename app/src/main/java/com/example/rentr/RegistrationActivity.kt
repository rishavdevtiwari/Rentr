package com.example.rentr

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.ui.theme.Blue
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.splash
import com.example.rentr.ui.theme.PurpleGrey80

class RegistrationActivity : ComponentActivity() {
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

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var confirmVisibility by remember { mutableStateOf(false) }

    val isFormFilled = email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()

    val context = LocalContext.current

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Black)
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                "Register Your ",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                style = TextStyle(
                    color = White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Left,
                    fontSize = 30.sp
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                " Account",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                style = TextStyle(
                    color = White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Left,
                    fontSize = 30.sp
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { data ->
                    email = data
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_email_24),
                        contentDescription = "Email Icon"
                    )
                },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                placeholder = {
                    Text("abc@gmail.com")
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Orange,
                    unfocusedContainerColor = splash,
                    focusedIndicatorColor = Orange,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = splash,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { data ->
                    password = data
                },
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (!visibility) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = {
                    IconButton(onClick = {
                        visibility = !visibility
                    }) {
                        Icon(
                            painter = if (visibility)
                                painterResource(R.drawable.baseline_visibility_off_24)
                            else
                                painterResource(R.drawable.baseline_visibility_24),
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                placeholder = {
                    Text("Enter password")
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Orange,
                    unfocusedContainerColor = splash,
                    focusedIndicatorColor = Orange,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { data ->
                    confirmPassword = data
                },
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (!confirmVisibility) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = {
                    IconButton(onClick = {
                        confirmVisibility = !confirmVisibility
                    }) {
                        Icon(
                            painter = if (confirmVisibility)
                                painterResource(R.drawable.baseline_visibility_off_24)
                            else
                                painterResource(R.drawable.baseline_visibility_24),
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                placeholder = {
                    Text("Confirm Password")
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Orange,
                    unfocusedContainerColor = splash,
                    focusedIndicatorColor = Orange,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
//                    if (isFormFilled) {
//                        if (context is android.app.Activity) {
//                            val intent = Intent(
//                                context, DashboardActivity::class.java
//                            )
//                            intent.putExtra("email", email)
//                            intent.putExtra("password", password)
//                            context.startActivity(intent)
//                        }
//                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormFilled) Orange else splash
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 15.dp, vertical = 20.dp),
            ) {
                Text("SignUp")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = White)) {
                        append("Already have an account?")
                    }

                    withStyle(style = SpanStyle(color = Blue)) {
                        append(" LogIn")
                    }
                }, modifier = Modifier
                    .clickable{
                    }
                    .padding(horizontal = 15.dp),
                style = TextStyle(fontSize = 16.sp)
            )
        }
    }
}

@Composable
fun SocialMediaCard(modifier: Modifier, image: Int, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = PurpleGrey80
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(label)
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    LoginBody()
}
