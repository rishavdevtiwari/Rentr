package com.example.rentr

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// region Palette
private val primaryColor = Color.Black
private val secondaryColor = Color(0xFF2A2A2A)
private val accentColor = Color(0xFFFF6200)
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
private val cardBackgroundColor = Color(0xFF2C2C2E)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current

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
                }
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            ProfileCard()
            Spacer(modifier = Modifier.height(30.dp))
            SettingsList()
        }
    }
}




@Composable
private fun ProfileCard() {
    val context = LocalContext.current
    val activity = context as? Activity
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, spotColor = Color(0x22000000), shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(primaryColor, secondaryColor)))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar()
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mr Nonchalant", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("mrnonchalant@gmail.com", color = textLightColor, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                PillBadge(text = "Verified", icon = Icons.Default.Person)
                PillBadge(text = "Active", icon = Icons.Default.Person) // Using Person as placeholder
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val intent = Intent(context,KYC::class.java)
                    activity?.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Update, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update KYC", color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun Avatar() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(accentColor, Color(0xFFFFC66C))))
            .padding(2.dp) // Simulates border
            .clip(CircleShape)
            .background(cardBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text("MN", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PillBadge(text: String, icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(50),
        color = cardBackgroundColor.copy(alpha = 0.5f),
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, color = textLightColor, fontSize = 12.sp)
        }
    }
}

data class SettingInfo(val icon: ImageVector, val title: String, val destination: Class<out Activity>? = null)

@Composable
private fun SettingsList() {
    val context = LocalContext.current
    val settings = listOf(
        SettingInfo(Icons.Default.Person, "Edit Profile", EditProfile::class.java),
        SettingInfo(Icons.Default.LocationOn, "Address"),
        SettingInfo(Icons.Default.Notifications, "Notification"),
        SettingInfo(Icons.Default.CreditCard, "Payment"),
        SettingInfo(Icons.Default.Lock, "Security", ChangePassActivity::class.java),
        SettingInfo(Icons.Default.Language, "Language"),
        SettingInfo(Icons.Default.Info, "Privacy Policy", PrivacyPolicyActivity::class.java),
        SettingInfo(Icons.AutoMirrored.Filled.HelpOutline, "Help Center", FaqScreenActivity::class.java),
        SettingInfo(Icons.Default.Group, "Invite Friends"),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        settings.forEachIndexed { index, setting ->
            SettingItem(icon = setting.icon, title = setting.title) {
                setting.destination?.let {
                    context.startActivity(Intent(context, it))
                }
            }
            if (index < settings.size - 1) {
                HorizontalDivider(color = Color(0xFF3A3A3A), thickness = 1.dp)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(onClick = { /*TODO*/ }) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Red)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Logout", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Medium)
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



@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
