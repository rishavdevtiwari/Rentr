package com.example.rentr.view

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.model.UserModel
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.viewmodel.UserViewModel
import java.util.Calendar

class FillProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Main surface using your BG40 color
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = BG40
            ) {
                FillProfileScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillProfileScreen() {
    // Form State
    var fullName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    //UserViewModel
    val userViewModel1 = remember { UserViewModel(UserRepoImp1()) }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = context as Activity

    //Capturing email and userId from intent
    val email = activity.intent?.getStringExtra("email") ?: ""
    val password = activity.intent?.getStringExtra("password") ?: ""

    // --- Date Picker Setup ---
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            dateOfBirth = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        },
        year,
        month,
        day
    )

    // --- Gender Picker Setup ---
    val genderOptions = listOf("Male", "Female", "Other")
    var expanded by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Fill Your Profile",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BG40
                )
            )
        },
        containerColor = BG40
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Profile Image Section ---
            Box(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(120.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Image",
                    tint = outline, // Used outline color for the empty state
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Field)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Orange)
                        .clickable { /* Handle Image Pick */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }


            // --- Input Fields ---
            RentrTextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Full Name"
            )
            Spacer(modifier = Modifier.height(20.dp))

            RentrTextField(
                value = email,
                onValueChange = {},
                placeholder = "Email",
                readOnly = true,
                trailingIcon = Icons.Default.Email
            )
            Spacer(modifier = Modifier.height(20.dp))

            // --- Date of Birth Picker ---
            Box(modifier = Modifier.clickable { datePickerDialog.show() }) {
                RentrTextField(
                    value = dateOfBirth,
                    onValueChange = {},
                    placeholder = "Date of Birth",
                    trailingIcon = Icons.Default.DateRange,
                    readOnly = true,
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            RentrTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = "Phone Number",
                keyboardType = KeyboardType.Phone
            )
            Spacer(modifier = Modifier.height(20.dp))

            // --- Gender Picker ---
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                RentrTextField(
                    modifier = Modifier.menuAnchor(),
                    value = gender,
                    onValueChange = {},
                    placeholder = "Gender",
                    trailingIcon = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    readOnly = true
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Field)
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Color.White) },
                            onClick = {
                                gender = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Continue Button ---
            Button(
                onClick = {
                    userViewModel1.register(email, password){ success, msg, userId ->
                        if(success){
                            val model = UserModel(
                                fullName = fullName,
                                gender = gender,
                                phoneNumber = phoneNumber,
                                dob = dateOfBirth,
                                email = email,
                                listings = mutableListOf("UNACCEPTED"),
                                verified = false,
                                kycUrl = mutableListOf()
                            )
                            userViewModel1.addUserToDatabase(userId, model) { success, msg->
                                if (success) {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    val intent = Intent(context, DashboardActivity::class.java)
                                    context.startActivity(intent)
                                    activity.finish()
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }

                            }
                        }else{
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }


                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- Custom TextField Component using your colors ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    enabled:Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        placeholder = { Text(text = placeholder, color = outline) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Field,
            unfocusedContainerColor = Field,
            disabledContainerColor = Field,
            focusedBorderColor = Orange,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White,
            cursorColor = Orange,
            selectionColors = TextSelectionColors(
                handleColor = Orange,
                backgroundColor = Orange.copy(alpha = 0.3f)
            ),
            focusedTrailingIconColor = Orange,
            unfocusedTrailingIconColor = outline,
            disabledTrailingIconColor = outline
        ),
        trailingIcon = trailingIcon?.let {
            {
                Icon(imageVector = it, contentDescription = null)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        readOnly = readOnly,
        singleLine = true
    )
}

@Preview
@Composable
fun FillProfilePreview() {
    FillProfileScreen()
}

// Dummy Color Vars to prevent Preview errors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val BG40 = Color(0xFF1E1E1E)
val outline = Color(0xFF818181)
val ButtonColor = Color(0xFFCCCCCC)
val promo = Color(0xFF85568D)
val splash = Color(0xFF2C2C2C)
val Blue = Color(0xFF144F7E)