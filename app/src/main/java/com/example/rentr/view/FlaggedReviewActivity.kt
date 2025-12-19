package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.R

// Color palette
private val primaryColor = Color(0xFF1E1E1E)
private val accentColor = Color(0xFFFF6200)
private val successColor = Color(0xFF4CAF50)
private val warningColor = Color(0xFFFF9800)
private val errorColor = Color(0xFFF44336)
private val infoColor = Color(0xFF2196F3)
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
private val cardBackgroundColor = Color(0xFF2C2C2E)

class FlagReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sample data - backend logic to implement later
        val flagId = intent.getStringExtra("flagId") ?: "FLAG_001"

        setContent {
            FlagReviewScreen(flagId = flagId)
        }
    }
}

// Data class for demo purposes - backend logic to implement later
data class DemoFlagReport(
    val id: String = "FLAG_001",
    val reportedUserId: String = "USER_456",
    val reportedUserName: String = "John Doe",
    val reportedUserEmail: String = "john@example.com",
    val reportedUserPhone: String = "+977 9841234567",
    val productId: String = "PROD_789",
    val productTitle: String = "Mountain Bike Pro",
    val productImage: Int = R.drawable.bicycle,
    val productCategory: String = "Sports Equipment",
    val productPrice: Double = 750.0,
    val flagType: String = "Inappropriate Content", // Scam, Fake Product, Inappropriate, Other
    val flagReason: String = "This product seems to be a scam. The seller is asking for payment outside the app and the images appear to be stolen from other websites.",
    val reporterId: String = "USER_123",
    val reporterName: String = "Sarah Smith",
    val reporterEmail: String = "sarah@example.com",
    val reporterPhone: String = "+977 9812345678",
    val reportedAt: String = "2023-12-15 14:30",
    val status: String = "pending" // pending, resolved, suspended
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagReviewScreen(flagId: String) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Demo data - backend logic to implement later
    val flagReport = remember {
        DemoFlagReport(
            id = flagId,
            reportedUserName = "John Doe",
            reportedUserEmail = "john@example.com",
            reportedUserPhone = "+977 9841234567",
            productTitle = "Mountain Bike Pro",
            productImage = R.drawable.bicycle,
            productCategory = "Sports Equipment",
            productPrice = 750.0,
            flagType = "Scam / Fraud",
            flagReason = "Seller asked for direct bank transfer outside the app. Phone number leads to different person. Images appear to be stolen from other websites.",
            reporterName = "Sarah Smith",
            reporterEmail = "sarah@example.com",
            reporterPhone = "+977 9812345678",
            reportedAt = "2023-12-15 14:30",
            status = "pending"
        )
    }

    var isLoading by remember { mutableStateOf(false) }

    // States for dialogs
    var showSuspendDialog by remember { mutableStateOf(false) }
    var showResolveDialog by remember { mutableStateOf(false) }
    var showMarkDialog by remember { mutableStateOf(false) }

    var suspendReason by remember { mutableStateOf("") }
    var resolveReason by remember { mutableStateOf("") }
    var markNote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Flag Review",
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accentColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Status Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (flagReport.status) {
                                    "resolved" -> successColor.copy(alpha = 0.9f)
                                    "suspended" -> errorColor.copy(alpha = 0.9f)
                                    else -> warningColor.copy(alpha = 0.9f)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (flagReport.status) {
                                "resolved" -> "RESOLVED"
                                "suspended" -> "SUSPENDED"
                                else -> "UNDER REVIEW"
                            },
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Reported User Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Reported User",
                                    tint = errorColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Reported User",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            FlagInfoRow(
                                icon = Icons.Default.Person,
                                label = "Name",
                                value = flagReport.reportedUserName,
                                iconColor = textLightColor
                            )

                            FlagInfoRow(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = flagReport.reportedUserEmail,
                                iconColor = textLightColor
                            )

                            FlagInfoRow(
                                icon = Icons.Default.Phone,
                                label = "Phone",
                                value = flagReport.reportedUserPhone,
                                iconColor = textLightColor
                            )

                            FlagInfoRow(
                                icon = Icons.Default.Person,
                                label = "User ID",
                                value = flagReport.reportedUserId,
                                iconColor = textLightColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Flagged Product Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.ShoppingBag,
                                    contentDescription = "Flagged Product",
                                    tint = warningColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Flagged Product",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Product Image
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = flagReport.productImage),
                                    contentDescription = flagReport.productTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = flagReport.productTitle,
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = flagReport.productCategory,
                                    color = textLightColor,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "NPR. ${"%.2f".format(flagReport.productPrice)}",
                                    color = accentColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FlagInfoRow(
                                icon = Icons.Default.Description,
                                label = "Product ID",
                                value = flagReport.productId,
                                iconColor = textLightColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Flag Details Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = "Flag Details",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Flag Details",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Flag Type with colored badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Flag Type",
                                    tint = when (flagReport.flagType) {
                                        "Scam / Fraud" -> errorColor
                                        "Fake Product" -> warningColor
                                        "Inappropriate Content" -> accentColor
                                        else -> infoColor
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (flagReport.flagType) {
                                                "Scam / Fraud" -> errorColor.copy(alpha = 0.2f)
                                                "Fake Product" -> warningColor.copy(alpha = 0.2f)
                                                "Inappropriate Content" -> accentColor.copy(alpha = 0.2f)
                                                else -> infoColor.copy(alpha = 0.2f)
                                            }
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = flagReport.flagType,
                                        color = when (flagReport.flagType) {
                                            "Scam / Fraud" -> errorColor
                                            "Fake Product" -> warningColor
                                            "Inappropriate Content" -> accentColor
                                            else -> infoColor
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Reason for Reporting",
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = flagReport.flagReason,
                                color = textLightColor,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            FlagInfoRow(
                                icon = Icons.Default.Description,
                                label = "Reported At",
                                value = flagReport.reportedAt,
                                iconColor = textLightColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reporter Information Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Report,
                                    contentDescription = "Reporter",
                                    tint = infoColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Reported By",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            FlagInfoRow(
                                icon = Icons.Default.Person,
                                label = "Name",
                                value = flagReport.reporterName,
                                iconColor = textLightColor
                            )

                            FlagInfoRow(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = flagReport.reporterEmail,
                                iconColor = textLightColor
                            )

                            FlagInfoRow(
                                icon = Icons.Default.Phone,
                                label = "Phone",
                                value = flagReport.reporterPhone,
                                iconColor = textLightColor
                            )

                            FlagInfoRow(
                                icon = Icons.Default.Person,
                                label = "User ID",
                                value = flagReport.reporterId,
                                iconColor = textLightColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons (only show if pending)
                    if (flagReport.status == "pending") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Suspend Button
                                Button(
                                    onClick = { showSuspendDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = errorColor)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Block,
                                            contentDescription = "Suspend",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Suspend",
                                            color = textColor,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                // Resolve Button
                                Button(
                                    onClick = { showResolveDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = successColor)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Resolve",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Resolve",
                                            color = textColor,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }

                            // Mark Button (full width)
                            Button(
                                onClick = { showMarkDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = warningColor)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Flag,
                                        contentDescription = "Mark",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mark for Further Review",
                                        color = textColor,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Show status message if already resolved/suspended
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when (flagReport.status) {
                                    "resolved" -> successColor.copy(alpha = 0.1f)
                                    "suspended" -> errorColor.copy(alpha = 0.1f)
                                    else -> cardBackgroundColor
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = when (flagReport.status) {
                                        "resolved" -> Icons.Default.CheckCircle
                                        else -> Icons.Default.Block
                                    },
                                    contentDescription = "Status",
                                    tint = when (flagReport.status) {
                                        "resolved" -> successColor
                                        else -> errorColor
                                    },
                                    modifier = Modifier.size(40.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = when (flagReport.status) {
                                        "resolved" -> "Flag Already Resolved"
                                        else -> "User Already Suspended"
                                    },
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = when (flagReport.status) {
                                        "resolved" -> "This flag report has been resolved"
                                        else -> "The reported user has been suspended"
                                    },
                                    color = textLightColor,
                                    fontSize = 14.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Suspend Dialog
        if (showSuspendDialog) {
            AlertDialog(
                onDismissRequest = { showSuspendDialog = false },
                title = {
                    Text(
                        text = "Suspend User",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Please provide a reason for suspending ${flagReport.reportedUserName}:",
                            color = textLightColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = suspendReason,
                            onValueChange = { newValue -> suspendReason = newValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = {
                                Text(
                                    text = "Enter suspension reason...",
                                    color = textLightColor.copy(alpha = 0.7f)
                                )
                            },
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = cardBackgroundColor,
                                unfocusedContainerColor = cardBackgroundColor,
                                disabledContainerColor = cardBackgroundColor,
                                focusedIndicatorColor = errorColor,
                                unfocusedIndicatorColor = textLightColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = false,
                            maxLines = 5
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Suspending will disable the user's account and remove their listings. User will be notified of the suspension.",
                            color = textLightColor,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showSuspendDialog = false
                                suspendReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardBackgroundColor
                            )
                        ) {
                            Text("Cancel", color = textColor)
                        }
                        Button(
                            onClick = {
                                // Backend logic to implement later
                                // Submit suspension with reason: suspendReason
                                showSuspendDialog = false
                                // Reset reason
                                suspendReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = errorColor
                            )
                        ) {
                            Text("Confirm Suspend", color = textColor)
                        }
                    }
                },
                containerColor = primaryColor,
                titleContentColor = textColor,
                textContentColor = textLightColor
            )
        }

        // Resolve Dialog
        if (showResolveDialog) {
            AlertDialog(
                onDismissRequest = { showResolveDialog = false },
                title = {
                    Text(
                        text = "Resolve Flag",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Please provide details on how this flag was resolved:",
                            color = textLightColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = resolveReason,
                            onValueChange = { newValue -> resolveReason = newValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = {
                                Text(
                                    text = "Enter resolution details...",
                                    color = textLightColor.copy(alpha = 0.7f)
                                )
                            },
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = cardBackgroundColor,
                                unfocusedContainerColor = cardBackgroundColor,
                                disabledContainerColor = cardBackgroundColor,
                                focusedIndicatorColor = successColor,
                                unfocusedIndicatorColor = textLightColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = false,
                            maxLines = 5
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Resolving will close this flag report and notify the reporter. The reported user and product will remain active.",
                            color = textLightColor,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showResolveDialog = false
                                resolveReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardBackgroundColor
                            )
                        ) {
                            Text("Cancel", color = textColor)
                        }
                        Button(
                            onClick = {
                                // Backend logic to implement later
                                // Submit resolution with details: resolveReason
                                showResolveDialog = false
                                // Reset reason
                                resolveReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = successColor
                            )
                        ) {
                            Text("Confirm Resolve", color = textColor)
                        }
                    }
                },
                containerColor = primaryColor,
                titleContentColor = textColor,
                textContentColor = textLightColor
            )
        }

        // Mark Dialog
        if (showMarkDialog) {
            AlertDialog(
                onDismissRequest = { showMarkDialog = false },
                title = {
                    Text(
                        text = "Mark for Review",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Add notes for further review:",
                            color = textLightColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = markNote,
                            onValueChange = { newValue -> markNote = newValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            placeholder = {
                                Text(
                                    text = "Enter review notes...",
                                    color = textLightColor.copy(alpha = 0.7f)
                                )
                            },
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = cardBackgroundColor,
                                unfocusedContainerColor = cardBackgroundColor,
                                disabledContainerColor = cardBackgroundColor,
                                focusedIndicatorColor = warningColor,
                                unfocusedIndicatorColor = textLightColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = false,
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Marking will keep this flag in pending state with your notes for later review.",
                            color = textLightColor,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showMarkDialog = false
                                markNote = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardBackgroundColor
                            )
                        ) {
                            Text("Cancel", color = textColor)
                        }
                        Button(
                            onClick = {
                                // Backend logic to implement later
                                // Add mark notes: markNote
                                showMarkDialog = false
                                // Reset note
                                markNote = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = warningColor
                            )
                        ) {
                            Text("Mark for Review", color = textColor)
                        }
                    }
                },
                containerColor = primaryColor,
                titleContentColor = textColor,
                textContentColor = textLightColor
            )
        }
    }
}

@Composable
fun FlagInfoRow(icon: ImageVector, label: String, value: String, iconColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                color = textLightColor,
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
fun FlagReviewScreenPreview() {
    FlagReviewScreen("FLAG_001")
}