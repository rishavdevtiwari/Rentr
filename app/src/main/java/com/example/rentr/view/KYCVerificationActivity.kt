package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
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

// Color palette (same as KYC screen for consistency)
private val primaryColor = Color(0xFF1E1E1E)
private val accentColor = Color(0xFFFF6200)
private val successColor = Color(0xFF4CAF50)
private val errorColor = Color(0xFFF44336)
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
private val cardBackgroundColor = Color(0xFF2C2C2E)

class KYCVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KYCVerificationScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KYCVerificationScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    // Sample document data - backend logic to implement later
    val documents = listOf(
        DocumentItem("Citizenship Front", "user_123_citizenship_front.jpg", Icons.Default.Description),
        DocumentItem("Citizenship Back", "user_123_citizenship_back.jpg", Icons.Default.Description),
        DocumentItem("Pan Card", "user_123_pan_card.jpg", Icons.Default.Description),
        DocumentItem("Bank A/c Details", "user_123_bank_details.pdf", Icons.Default.Description),
        DocumentItem("Profile Pic", "user_123_profile.jpg", Icons.Default.AccountCircle)
    )

    var isLoading by remember { mutableStateOf(false) }

    // State for rejection dialog
    var showRejectionDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "KYC Verification",
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
            ) {
                // User info section
                UserInfoSection()

                // Scrollable documents section
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(documents) { document ->
                        DocumentCard(document)
                    }
                }

                // Action buttons
                ActionButtonsSection(
                    onRejectClick = { showRejectionDialog = true },
                    onAcceptClick = {
                        // Backend logic to implement later
                        // Handle KYC acceptance
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        // Rejection Reason Dialog
        if (showRejectionDialog) {
            AlertDialog(
                onDismissRequest = { showRejectionDialog = false },
                title = {
                    Text(
                        text = "Reason for KYC Rejection",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Please provide a reason for rejecting this KYC verification:",
                            color = textLightColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = rejectionReason,
                            onValueChange = { rejectionReason = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = {
                                Text(
                                    text = "Enter rejection reason...",
                                    color = textLightColor.copy(alpha = 0.7f)
                                )
                            },
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = cardBackgroundColor,
                                unfocusedContainerColor = cardBackgroundColor,
                                disabledContainerColor = cardBackgroundColor,
                                focusedIndicatorColor = accentColor,
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
                            text = "Common reasons: Blurry documents, missing information, expired documents, mismatched details, poor quality images, etc.",
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
                                showRejectionDialog = false
                                rejectionReason = ""
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
                                // Submit KYC rejection with reason: rejectionReason
                                showRejectionDialog = false
                                // Reset reason
                                rejectionReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = errorColor
                            )
                        ) {
                            Text("Submit Rejection", color = textColor)
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
private fun UserInfoSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile image placeholder - using Icon instead of Image with painterResource
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3A3C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.size(40.dp),
                        tint = textLightColor
                    )
                }

                Column {
                    Text(
                        text = "John Doe",
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "User ID: USR-001234",
                        color = textLightColor,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Submitted: 12 Dec 2023",
                        color = textLightColor,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(document: DocumentItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Document label with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    document.icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = document.label,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // File name
            Text(
                text = document.fileName,
                color = textLightColor,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Image preview placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF3A3A3C))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder for document preview
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            document.icon,
                            contentDescription = "Document",
                            modifier = Modifier.size(50.dp),
                            tint = textLightColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Document Preview",
                            color = textLightColor,
                            fontSize = 14.sp
                        )
                    }
                }

                // Status badge (if verified/rejected)
                if (document.status != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (document.status) {
                                    DocumentStatus.VERIFIED -> successColor
                                    DocumentStatus.REJECTED -> errorColor
                                    DocumentStatus.PENDING -> accentColor
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = document.status.name,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons for this specific document
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* View full document - backend logic to implement later */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3A3A3C)
                    )
                ) {
                    Text(
                        text = "View Full",
                        color = textColor,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = { /* Zoom document - backend logic to implement later */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3A3A3C)
                    )
                ) {
                    Text(
                        text = "Zoom",
                        color = textColor,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onRejectClick: () -> Unit,
    onAcceptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Reject button
        Button(
            onClick = onRejectClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = errorColor
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Reject",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reject KYC",
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }

        // Accept button
        Button(
            onClick = onAcceptClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = successColor
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Accept",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Accept KYC",
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// Data classes
data class DocumentItem(
    val label: String,
    val fileName: String,
    val icon: ImageVector = Icons.Default.Description,
    val status: DocumentStatus? = null
)

enum class DocumentStatus {
    VERIFIED, REJECTED, PENDING
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
fun KYCVerificationScreenPreview() {
    KYCVerificationScreen()
}