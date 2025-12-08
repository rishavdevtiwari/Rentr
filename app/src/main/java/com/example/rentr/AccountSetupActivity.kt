package com.example.rentr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange

// --- YOUR COLOR PALETTE ---
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Field = Color(0xFF333232)
val BG40 = Color(0xFF1E1E1E) // Changed to 0xFF for full opacity (Solid color)
val Orange = Color(0xFFFF5D18)

val outline = Color(0xFF818181)
val ButtonColor = Color(0xFFCCCCCC) // Renamed to ButtonColor to avoid conflict with Button composable

val promo = Color(0xFF85568D)
val splash = Color(0xFF2C2C2C)
val Blue = Color(0xFF144F7E)

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
    var nickname by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = context as? Activity

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
                // Profile Placeholder
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Image",
                    tint = outline, // Used outline color for the empty state
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Field)
                )

                // Edit Icon (Pencil)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Orange) // Your Orange Accent
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

//            RentrTextField(
//                value = nickname,
//                onValueChange = { nickname = it },
//                placeholder = "Nickname"
//            )
//            Spacer(modifier = Modifier.height(20.dp))

            RentrTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                placeholder = "Date of Birth",
                trailingIcon = Icons.Default.DateRange
            )
            Spacer(modifier = Modifier.height(20.dp))

            RentrTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                trailingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Phone Number Row
            Row(modifier = Modifier.fillMaxWidth()) {
//                // Country Code (Static simulation)
//                Row(
//                    modifier = Modifier
//                        .height(56.dp)
//                        .background(Field, RoundedCornerShape(12.dp))
//                        .padding(horizontal = 12.dp)
//                        .clickable { },
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Flag,
//                        contentDescription = "Flag",
//                        tint = Color.White,
//                        modifier = Modifier.size(24.dp)
//                    )
//                    Icon(
//                        imageVector = Icons.Default.ArrowDropDown,
//                        contentDescription = null,
//                        tint = outline
//                    )
                //}


                // Phone Input
                RentrTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = "Phone Number",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Phone
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            RentrTextField(
                value = gender,
                onValueChange = { },
                placeholder = "Gender",
                trailingIcon = Icons.Default.ArrowDropDown,
                readOnly = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- Continue Button ---
            Button(
                onClick = {
                    Toast.makeText(context, "Logged In.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, DashboardActivity::class.java)
                    context.startActivity(intent)
                    activity?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange, // Your primary Orange
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp
                )
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
    readOnly: Boolean = false
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(text = placeholder, color = outline) // Using your 'outline' color for hint
        },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Field,
            focusedIndicatorColor = Orange,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.White,
            focusedTrailingIconColor = Color.Black,
            unfocusedTrailingIconColor = Color.White
        ),
        trailingIcon = trailingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null
                )
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