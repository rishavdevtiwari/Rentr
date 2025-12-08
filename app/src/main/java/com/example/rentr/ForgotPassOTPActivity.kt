package com.example.rentr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.ui.theme.Button
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange

class ForgotPassOTPActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForgotPassOTPBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPassOTPBody() {
    var otp by remember { mutableStateOf("") }
    // Button is enabled only when the OTP field is not blank
    val isEnabled = otp.isNotBlank() && otp.length == 6 // Assuming 6-digit OTP
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Fix for ClassCastException in Preview: Safely cast context to Activity
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
                    IconButton(onClick = {
                        val intent = Intent(context,ForgotPassGmailActivity::class.java)
                        context.startActivity(intent)
                        activity?.finish()
                    }) {
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

            Spacer(modifier = Modifier.height(100.dp))
            Column() {
                Text(
                    text = "Enter the OTP sent over to your gmail.",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(50.dp))

                // OTP Field
                OutlinedTextField(
                    value = otp,
                    onValueChange = { data ->
                        if (data.length <= 6) { // Limit input to 6 digits
                            otp = data
                        }
                    },
                    leadingIcon = {
                        // Using a placeholder icon; replace with your R.drawable.otp_icon or similar
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_lock_24),
                            contentDescription = "OTP Icon",
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    placeholder = {
                        Text(text = "Enter 6-digit OTP", color = Color.Gray)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Field,
                        focusedIndicatorColor = Orange,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        focusedLeadingIconColor = Color.Black,
                        unfocusedLeadingIconColor = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(70.dp))

            // Submit Button
            Button(
                onClick = {
                    if(otp.length!==6){
                        Toast.makeText(context, "Please enter a valid OTP", Toast.LENGTH_SHORT).show()
                    }else{
                        val intent = Intent(context, ForgotPassNewPassActivity::class.java)
                        context.startActivity(intent)
                        activity?.finish()
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
                Text("Verify", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPassOTPPreview() {
    ForgotPassOTPBody()
}