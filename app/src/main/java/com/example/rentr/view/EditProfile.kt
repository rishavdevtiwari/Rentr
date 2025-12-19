package com.example.rentr.view

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.UserViewModel
import java.util.Calendar

// region Palette
private val primaryColor = Color(0xFF1E1E1E)
private val accentColor = Color(0xFFFF6200)
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
private val cardBackgroundColor = Color(0xFF2C2C2E)
// endregion

class EditProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                val userViewModel = remember { UserViewModel(UserRepoImp1()) }
                EditProfileScreen(userViewModel = userViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(userViewModel: UserViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    val user by userViewModel.user.observeAsState()

    // Local state for the input fields
    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    // Fetch user data when the screen is first composed
    LaunchedEffect(Unit) {
        userViewModel.getCurrentUser()?.uid?.let { userId ->
            userViewModel.getUserById(userId) { _, _, _ ->
                // LiveData observer will handle the update
            }
        }
    }

    // Populate the fields when user data is loaded
    LaunchedEffect(user) {
        user?.let {
            fullName = it.fullName
            dob = it.dob
            email = it.email
            phoneNumber = it.phoneNumber
            gender = it.gender
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            dob = "${month + 1}/$dayOfMonth/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        activity?.finish() // Just finish, don't send an OK result
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor
                )
            )
        },
        containerColor = primaryColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            ProfileTextField(label = "Full Name", value = fullName, onValueChange = { fullName = it })
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTextField(
                label = "Date of Birth",
                value = dob,
                onValueChange = { dob = it },
                readOnly = true,
                trailingIcon = { IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Default.CalendarToday, contentDescription = "Select Date") } }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTextField(label = "Email", value = email, onValueChange = {}, readOnly = true)
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTextField(label = "Phone Number", value = phoneNumber, onValueChange = { phoneNumber = it })
            Spacer(modifier = Modifier.height(16.dp))
            ProfileDropdownField(label = "Gender", selectedValue = gender, onValueChange = { gender = it }, options = listOf("Rather Not Say", "Male", "Female"))

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val currentUserId = userViewModel.getCurrentUser()?.uid
                    val currentUserData = user

                    if (currentUserId != null && currentUserData != null) {
                        val updatedUser = currentUserData.copy(
                            fullName = fullName,
                            dob = dob,
                            phoneNumber = phoneNumber,
                            gender = gender,
                        )
                        userViewModel.updateProfile(currentUserId, updatedUser) { success, msg ->
                            if (success) {
                                Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                                // Set the result and finish the activity
                                activity?.setResult(Activity.RESULT_OK)
                                activity?.finish()
                            } else {
                                Toast.makeText(context, "Update failed: $msg", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Could not update profile. User not found.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Update", color = textColor, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit, readOnly: Boolean = false, trailingIcon: @Composable (() -> Unit)? = null) {
    Column {
        Text(text = label, color = textLightColor, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            readOnly = readOnly,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cardBackgroundColor,
                unfocusedContainerColor = cardBackgroundColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = accentColor,
                focusedTrailingIconColor = textLightColor,
                unfocusedTrailingIconColor = textLightColor
            ),
            trailingIcon = trailingIcon
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDropdownField(label: String, selectedValue: String, onValueChange: (String) -> Unit, options: List<String>) {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        Text(text = label, color = textLightColor, modifier = Modifier.padding(bottom = 8.dp))
        ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = it }) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {}, // Read-only
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = cardBackgroundColor,
                    unfocusedContainerColor = cardBackgroundColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTrailingIconColor = textLightColor,
                    unfocusedTrailingIconColor = textLightColor
                ),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.background(cardBackgroundColor)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = textColor) },
                        onClick = {
                            onValueChange(option)
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}
