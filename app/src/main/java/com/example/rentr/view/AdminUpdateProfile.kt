package com.example.rentr.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.R
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.ui.theme.splash

data class AdminProfile(
    val name: String,
    val age: Int,
    val qualification: String,
    val profileImage: Int
)

class AdminUpdateProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                UpdateAdminProfileScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateAdminProfileScreen() {
    // In a real app, you\'d receive the current admin profile, e.g., via Intent.
    // For now, we\'ll use a sample profile.
    val currentProfile = remember {
        mutableStateOf(
            AdminProfile(
                name = "Alex Doe",
                age = 34,
                qualification = "Head of Operations",
                profileImage = R.drawable.ic_launcher_background // Replace with your image
            )
        )
    }

    var name by remember { mutableStateOf(currentProfile.value.name) }
    var age by remember { mutableStateOf(currentProfile.value.age.toString()) }
    var qualification by remember { mutableStateOf(currentProfile.value.qualification) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = splash,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = splash
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Image(
                painter = painterResource(id = currentProfile.value.profileImage),
                contentDescription = "Admin Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Text Fields
            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = qualification,
                onValueChange = { qualification = it },
                label = { Text("Qualification") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    // Here you would typically save the updated profile to your backend or local storage.
                    // For this example, we can just update the local state.
                    val newAge = age.toIntOrNull() ?: currentProfile.value.age
                    currentProfile.value = currentProfile.value.copy(
                        name = name,
                        age = newAge,
                        qualification = qualification
                    )
                    // Optionally, you could finish the activity after saving.
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Text("Save Changes", fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateAdminProfileScreenPreview() {
    RentrTheme {
        UpdateAdminProfileScreen()
    }
}
