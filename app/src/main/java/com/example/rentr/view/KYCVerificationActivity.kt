package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.viewmodel.UserViewModel

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
        val userId = intent.getStringExtra("userId") ?: ""

        setContent {
            KYCVerificationScreen(userId = userId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KYCVerificationScreen(userId: String) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Initialize ViewModel
    val userViewModel: UserViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return UserViewModel(UserRepoImp1()) as T
            }
        }
    )

    // State variables
    var userData by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showRejectionDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }

    // Fetch user data when screen loads
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId) { success, _, user ->
                if (success && user != null) {
                    // Transform to UserData
                    userData = UserData(
                        id = user.userId,
                        name = user.fullName,
                        email = user.email,
                        phone = user.phoneNumber ?: "Not provided",
                        profileImage = user.profileImage,
                        verified = user.verified,
                        documents = getSampleDocumentsForUser(userId)
                    )
                }
                isLoading = false
            }
        }
    }

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
        } else if (userData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("User not found", color = textColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // User info section
                UserInfoSection(userData = userData!!)

                // Scrollable documents section
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(userData!!.documents) { document ->
                        DocumentCard(document)
                    }
                }

                // Action buttons
                ActionButtonsSection(
                    onRejectClick = { showRejectionDialog = true },
                    onAcceptClick = {
                        // TODO: Implement KYC acceptance
                        // userViewModel.verifyUserKYC(userId, true, "") { ... }
                        activity?.finish() // Go back after approval
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
                                // TODO: Implement KYC rejection
                                // userViewModel.verifyUserKYC(userId, false, rejectionReason) { ... }
                                showRejectionDialog = false
                                rejectionReason = ""
                                activity?.finish() // Go back to admin dashboard
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
private fun UserInfoSection(userData: UserData) {
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
                // Profile image
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = userData.profileImage,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        placeholder = @androidx.compose.runtime.Composable {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(40.dp),
                                tint = textLightColor
                            )
                        } as Painter?,
                        error = @androidx.compose.runtime.Composable {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(40.dp),
                                tint = textLightColor
                            )
                        } as Painter?
                    )
                }

                Column {
                    Text(
                        text = userData.name,
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "User ID: ${userData.id}",
                        color = textLightColor,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Email: ${userData.email}",
                        color = textLightColor,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Phone: ${userData.phone}",
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

            // Document preview placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF3A3A3C)),
                contentAlignment = Alignment.Center
            ) {
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
                    Text(
                        text = "(Would show actual document)",
                        color = textLightColor.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons for this specific document
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* TODO: Implement view full document */ },
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
                    onClick = { /* TODO: Implement zoom document */ },
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

// Helper function to get documents
private fun getSampleDocumentsForUser(userId: String): List<DocumentItem> {
    return listOf(
        DocumentItem("Citizenship Front", "citizenship_front.jpg", Icons.Default.Description),
        DocumentItem("Citizenship Back", "citizenship_back.jpg", Icons.Default.Description),
        DocumentItem("PAN Card", "pan_card.jpg", Icons.Default.Description),
        DocumentItem("Bank Details", "bank_details.pdf", Icons.Default.Description),
        DocumentItem("Profile Photo", "profile.jpg", Icons.Default.AccountCircle)
    )
}

// Data classes
data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val profileImage: String,
    val verified: Boolean,
    val documents: List<DocumentItem>
)

data class DocumentItem(
    val label: String,
    val fileName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Description,
    val status: DocumentStatus? = null
)

enum class DocumentStatus {
    VERIFIED, REJECTED, PENDING
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
fun KYCVerificationScreenPreview() {
    KYCVerificationScreen(userId = "test_user_123")
}