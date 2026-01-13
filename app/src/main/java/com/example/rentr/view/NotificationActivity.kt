package com.example.rentr.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.model.NotificationModel
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationScreen(onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBack: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val notifications = remember { mutableStateListOf<NotificationModel>() }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch Notifications from Realtime Database
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            val ref = FirebaseDatabase.getInstance().getReference("notifications").child(currentUser.uid)

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notifications.clear()
                    for (child in snapshot.children) {
                        val notif = child.getValue(NotificationModel::class.java)
                        if (notif != null) notifications.add(notif)
                    }
                    // Sort by newest first (Timestamp descending)
                    notifications.sortByDescending { it.timestamp }
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(notifications) { notif ->
                    NotificationItem(notif)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notif: NotificationModel) {
    // Format timestamp to readable date
    val date = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(notif.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = Field),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(notif.title, color = Orange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(date, color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(notif.body, color = Color.White, fontSize = 14.sp)
        }
    }
}