package com.example.rentr.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rentr.R
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.ui.theme.splash

class AdminSettings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                AdminSettingsScreen()
            }
        }
    }
}

data class Admin(
    val name: String,
    val age: Int,
    val qualification: String,
    val profileImage: Int // Using a drawable resource for the image
) {
    companion object {
        val Saver = mapSaver(
            save = { mapOf("name" to it.name, "age" to it.age, "qualification" to it.qualification, "profileImage" to it.profileImage) },
            restore = { savedMap ->
                Admin(
                    name = savedMap["name"] as String,
                    age = savedMap["age"] as Int,
                    qualification = savedMap["qualification"] as String,
                    profileImage = savedMap["profileImage"] as Int
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen() {
    var admin by rememberSaveable(stateSaver = Admin.Saver) {
        mutableStateOf(
            Admin(
                name = "Alex Doe",
                age = 34,
                qualification = "Head of Operations",
                profileImage = R.drawable.ic_launcher_background // Replace with your actual admin image
            )
        )
    }
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditProfileDialog(
            admin = admin,
            onDismiss = { showEditDialog = false },
            onSave = {
                admin = it
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            AdminProfileCard(admin = admin)
            Spacer(modifier = Modifier.height(32.dp))
            SettingsMenuList(onProfileUpdateClick = { showEditDialog = true })
        }
    }
}

@Composable
fun AdminProfileCard(admin: Admin) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3A3A3A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = admin.profileImage),
                contentDescription = "Admin Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = admin.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${admin.age} years old", fontSize = 14.sp, color = Color.LightGray)
                Text(text = admin.qualification, fontSize = 14.sp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun SettingsMenuList(onProfileUpdateClick: () -> Unit) {
    Column {
        SettingsMenuItem(icon = Icons.Outlined.Person, title = "Profile Update", onClick = onProfileUpdateClick)
        Divider(color = Color.Gray)
        SettingsMenuItem(icon = Icons.Outlined.Notifications, title = "Notifications")
        Divider(color = Color.Gray)
        SettingsMenuItem(icon = Icons.Outlined.Lock, title = "Privacy Policy")
        Divider(color = Color.Gray)
        SettingsMenuItem(icon = Icons.AutoMirrored.Filled.Logout, title = "Logout", isDestructive = true)
    }
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val titleColor = if (isDestructive) Color(0xFFE63946) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = titleColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = titleColor, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
fun EditProfileDialog(
    admin: Admin,
    onDismiss: () -> Unit,
    onSave: (Admin) -> Unit
) {
    var name by remember { mutableStateOf(admin.name) }
    var age by remember { mutableStateOf(admin.age.toString()) }
    var qualification by remember { mutableStateOf(admin.qualification) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.Gray,
        cursorColor = Color.White,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.LightGray
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Update Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = qualification,
                    onValueChange = { qualification = it },
                    label = { Text("Qualification") },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val updatedAdmin = admin.copy(
                            name = name,
                            age = age.toIntOrNull() ?: admin.age,
                            qualification = qualification
                        )
                        onSave(updatedAdmin)
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminSettingsScreenPreview() {
    RentrTheme {
        AdminSettingsScreen()
    }
}
