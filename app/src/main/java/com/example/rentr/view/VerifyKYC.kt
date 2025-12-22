package com.example.rentr.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.ui.theme.RentrTheme

// --- 1. DATA MODELS & ENUMS ---
enum class KycStatus(val displayName: String, val color: Color) {
    Pending("Pending", Color(0xFFFFA500)), // Orange
    Approved("Approved", Color(0xFF4CAF50)), // Green
    Rejected("Rejected", Color(0xFFE63946))  // Red
}

data class KycRequest(
    val id: String,
    val userName: String,
    val userEmail: String,
    val documentUrl: String, // Placeholder for document link
    var status: KycStatus
)

class VerifyKYC : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                KycVerificationScreen()
            }
        }
    }
}

// --- 2. MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycVerificationScreen() {
    val kycRequests = remember {
        mutableStateListOf(
            KycRequest("1", "John Snow", "john.s@example.com", "", KycStatus.Pending),
            KycRequest("2", "Daenerys Targaryen", "dany.t@example.com", "", KycStatus.Pending),
            KycRequest("3", "Tyrion Lannister", "tyrion.l@example.com", "", KycStatus.Approved),
            KycRequest("4", "Cersei Lannister", "cersei.l@example.com", "", KycStatus.Rejected)
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    val displayedRequests = when (selectedTab) {
        1 -> kycRequests.filter { it.status == KycStatus.Pending }
        else -> kycRequests
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KYC Verification", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            KycListControls(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedRequests) { request ->
                    KycRequestCard(
                        request = request,
                        onApprove = {
                            val index = kycRequests.indexOfFirst { it.id == request.id }
                            if (index != -1) {
                                kycRequests[index] = request.copy(status = KycStatus.Approved)
                            }
                        },
                        onReject = {
                            val index = kycRequests.indexOfFirst { it.id == request.id }
                            if (index != -1) {
                                kycRequests[index] = request.copy(status = KycStatus.Rejected)
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- 3. COMPONENT PARTS ---
@Composable
fun KycListControls(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("All Users", "Unverified")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            Column(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .clickable { onTabSelected(index) }
            ) {
                Text(
                    text = title,
                    fontSize = if (selectedTab == index) 20.sp else 16.sp,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedTab == index) MaterialTheme.colorScheme.onBackground else Color.Gray
                )
                if (selectedTab == index) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(2.dp)
                            .width(20.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(2.dp)
                            )
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun KycRequestCard(
    request: KycRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3A3A3A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "User",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = request.userName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Text(text = request.userEmail, fontSize = 14.sp, color = Color.LightGray)
                }
                StatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            if (request.status == KycStatus.Pending) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { /* Handle document view */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Gray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("View Document")
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    }
                }
            }
        }
    }


@Composable
fun StatusBadge(status: KycStatus) {
    Surface(
        color = status.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = status.displayName,
            color = status.color,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

// --- 4. PREVIEW ---
@Preview(showBackground = true, name = "KYC Dark Mode", device = "spec:width=411dp,height=891dp")
@Composable
fun KycVerificationScreenPreview() {
    val darkColors = darkColorScheme(
        primary = Color(0xFF4CAF50),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(colorScheme = darkColors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            KycVerificationScreen()
        }
    }
}
