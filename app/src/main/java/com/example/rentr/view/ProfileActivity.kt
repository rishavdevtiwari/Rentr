package com.example.rentr.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.rentr.model.UserModel
import com.example.rentr.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// region Palette
private val primaryColor = Color.Black
private val secondaryColor = Color(0xFF2A2A2A)
private val accentColor = Color(0xFFFF6200)
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
private val cardBackgroundColor = Color(0xFF2C2C2E)
private val pendingColor = Color(0xFFFFC107)
private val successColor = Color(0xFF4CAF50)
private val errorColor = Color(0xFFF44336)
private val warningColor = Color(0xFFFF9800)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(userViewModel: UserViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val user by userViewModel.user.observeAsState()
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                isLoading = true
                val newImageUrl = uploadProfileImage(uri)
                if (newImageUrl != null) {
                    val uid = userViewModel.getCurrentUser()?.uid
                    if (uid != null) {
                        userViewModel.updateProfileImage(
                            userId = uid,
                            imageUrl = newImageUrl
                        ) { _, _ ->
                            isLoading = false
                        }
                    } else {
                        isLoading = false
                    }
                } else {
                    isLoading = false
                }
            }
        }
    }

    val editProfileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            userViewModel.getCurrentUser()?.uid?.let { userId ->
                userViewModel.getUserById(userId) { _, _, _ -> }
            }
        }
    }

    LaunchedEffect(Unit) {
        userViewModel.getCurrentUser()?.uid?.let { userId ->
            userViewModel.getUserById(userId) { _, _, _ -> }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "More options",
                            tint = textLightColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = primaryColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            ProfileCard(user, isLoading) { imagePickerLauncher.launch("image/*") }
            Spacer(modifier = Modifier.height(30.dp))
            SettingsList(onEditProfile = {
                val intent = Intent(context, EditProfile::class.java)
                editProfileLauncher.launch(intent)
            },userViewModel)
        }
    }
}

@Composable
private fun ProfileCard(user: UserModel?, isLoading: Boolean, onAvatarClick: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x22000000),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(primaryColor, secondaryColor)))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(user = user, isLoading = isLoading, onClick = onAvatarClick)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user?.fullName ?: "...", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(user?.email ?: "", color = textLightColor, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                user?.let {
                    PillBadge(isVerified = it.verified)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // KYC Status Logic
            user?.let {
                val hasKYC = it.kycUrl.isNotEmpty()

                when {
                    it.verified -> {
                        Button(
                            onClick = { /* View KYC status - already verified */ },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = successColor,
                                disabledContentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("KYC Verified", color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    hasKYC && !it.verified -> {
                        Button(
                            onClick = { /* Non-clickable status indicator */ },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = pendingColor,
                                disabledContentColor = Color.Black
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("KYC: Under Review", fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    else -> {
                        Button(
                            onClick = {
                                val intent = Intent(context, KYC::class.java)
                                activity?.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Update, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Complete KYC",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Avatar(user: UserModel?, isLoading: Boolean, onClick: () -> Unit) {
    fun getInitials(name: String): String {
        val names = name.trim().split(" ")
        return if (names.size > 1) {
            "${names.first().firstOrNull() ?: ""}${names.last().firstOrNull() ?: ""}".uppercase()
        } else {
            (name.firstOrNull()?.toString() ?: "").uppercase()
        }
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(accentColor, Color(0xFFFFC66C))))
            .padding(2.dp)
            .clip(CircleShape)
            .background(cardBackgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!user?.profileImage.isNullOrEmpty()) {
            AsyncImage(
                model = user?.profileImage,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Text(
                text = user?.fullName?.let { getInitials(it) } ?: "...",
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = accentColor,
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
private fun PillBadge(isVerified: Boolean) {
    if (isVerified){}else{
        val text = if (isVerified) "Verified" else "Unverified"
        val icon = if (isVerified) Icons.Default.CheckCircle else Icons.Default.HighlightOff
        val backgroundColor = if (isVerified) cardBackgroundColor.copy(alpha = 0.5f) else Color.Red
        val iconColor = if (isVerified) accentColor else Color.White
        val textColor = if (isVerified) textLightColor else Color.White

        Surface(
            shape = RoundedCornerShape(50),
            color = backgroundColor,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = text, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text, color = textColor, fontSize = 12.sp)
            }
        }
    }
}

data class SettingInfo(val icon: ImageVector, val title: String, val destination: Class<out Activity>? = null, val action: (() -> Unit)? = null)

@Composable
private fun SettingsList(onEditProfile: () -> Unit, userViewModel : UserViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    val settings = listOf(
        SettingInfo(Icons.Default.Person, "Edit Profile", action = onEditProfile),
        SettingInfo(Icons.Default.LocationOn, "Address"),
        SettingInfo(Icons.Default.Notifications, "Notification"),
        SettingInfo(Icons.Default.CreditCard, "Payment"),
        SettingInfo(Icons.Default.Lock, "Security", destination = ChangePassActivity::class.java),
        SettingInfo(Icons.Default.Language, "Language"),
        SettingInfo(Icons.Default.Info, "Privacy Policy", destination = PrivacyPolicyActivity::class.java),
        SettingInfo(Icons.AutoMirrored.Filled.HelpOutline, "Help Center", destination = FaqScreenActivity::class.java),
        SettingInfo(Icons.Default.Group, "Invite Friends"),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        settings.forEachIndexed { index, setting ->
            SettingItem(icon = setting.icon, title = setting.title) {
                if (setting.action != null) {
                    setting.action.invoke()
                } else {
                    setting.destination?.let {
                        context.startActivity(Intent(context, it))
                    }
                }
            }
            if (index < settings.size - 1) {
                HorizontalDivider(color = Color(0xFF3A3A3A), thickness = 1.dp)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(onClick = {
                val sharedPreferences = context.getSharedPreferences("rentr_prefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putBoolean("remember_me", false)
                    apply()
                }
                userViewModel.logout { success,message ->
                    if(success){
                        Toast.makeText(context, "Logged Out!", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(context,"Unsuccessful logout!",Toast.LENGTH_SHORT).show()
                    }
                }
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                activity?.finish()
        }) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Red)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Logout", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        TextButton(onClick = {
            val currentUserId = userViewModel.getCurrentUser()?.uid
            if (currentUserId != null) {
                userViewModel.deleteAccount(currentUserId) { _, _ ->
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    activity?.finish()
                }
            }
        }) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Delete Account", tint = Color.Red)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Delete Account", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

    }
}

@Composable
fun SettingItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        if (title == "Language") {
            Text("English (US)", color = textLightColor, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = textLightColor.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

private suspend fun uploadProfileImage(uri: Uri): String? {
    return suspendCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    continuation.resume(resultData?.get("secure_url")?.toString())
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resume(null)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    continuation.resume(null)
                }
            })
            .dispatch()
    }
}
