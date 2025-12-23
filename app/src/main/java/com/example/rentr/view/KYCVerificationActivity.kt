package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.rentr.model.KYCStatus
import com.example.rentr.model.UserModel
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Color palette
private val primaryColor = Color(0xFF1E1E1E)
private val accentColor = Color(0xFFFF6200)
private val successColor = Color(0xFF4CAF50)
private val errorColor = Color(0xFFF44336)
private val warningColor = Color(0xFFFFC107)
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
private val cardBackgroundColor = Color(0xFF2C2C2E)
private val pendingColor = Color(0xFFFF9800)

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
    var userData by remember { mutableStateOf<UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var kycDetails by remember { mutableStateOf<Map<String, KYCStatus>?>(null) }
    var showRejectionDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }
    var showApprovalDialog by remember { mutableStateOf(false) }

    // Calculate KYC statistics
    val kycStats = remember(kycDetails) {
        if (kycDetails.isNullOrEmpty()) {
            Triple(0, 0, 0) // pending, approved, rejected
        } else {
            val pending = kycDetails!!.values.count { it.status == "pending" }
            val approved = kycDetails!!.values.count { it.status == "approved" }
            val rejected = kycDetails!!.values.count { it.status == "rejected" }
            Triple(pending, approved, rejected)
        }
    }

    // Fetch user data and KYC details
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId) { success, _, user ->
                if (success && user != null) {
                    userData = user

                    // Check if user has KYC details
                    if (user.kycDetails.isNotEmpty()) {
                        kycDetails = user.kycDetails
                    } else if (user.kycUrl.isNotEmpty()) {
                        // Convert old structure to new structure
                        val convertedDetails = convertOldKYCToNew(user.kycUrl)
                        kycDetails = convertedDetails

                        // Update the database with new structure
                        if (convertedDetails.isNotEmpty()) {
                            val updatedUser = user.copy(kycDetails = convertedDetails)
                            userViewModel.updateProfile(userId, updatedUser) { _, _ ->
                                // Log for debugging
                                Log.d("KYCVerification", "Converted old KYC structure to new")
                            }
                        }
                    }
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

                // KYC Status Summary
                KYCStatusSummary(
                    pending = kycStats.first,
                    approved = kycStats.second,
                    rejected = kycStats.third,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Scrollable documents section
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (kycDetails.isNullOrEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = "No documents",
                                        tint = textLightColor,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        "No KYC documents found",
                                        color = textLightColor,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "User hasn't uploaded any KYC documents yet",
                                        color = textLightColor.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Define document types with their display info
                        val documentTypes = listOf(
                            DocumentTypeInfo("citizenship_front", "Citizenship Front", Icons.Default.Badge),
                            DocumentTypeInfo("citizenship_back", "Citizenship Back", Icons.Default.Badge),
                            DocumentTypeInfo("pan", "PAN Card", Icons.Default.CreditCard),
                            DocumentTypeInfo("bank", "Bank Details", Icons.Default.AccountBalance),
                            DocumentTypeInfo("profile", "Profile Photo", Icons.Default.AccountCircle)
                        )

                        items(documentTypes) { docTypeInfo ->
                            val kycStatus = kycDetails?.get(docTypeInfo.type)

                            if (kycStatus != null) {
                                KYCDocumentCard(
                                    documentName = docTypeInfo.displayName,
                                    documentType = docTypeInfo.type,
                                    kycStatus = kycStatus,
                                    icon = docTypeInfo.icon,
                                    onApprove = {
                                        userViewModel.updateKYCStatus(userId, docTypeInfo.type, "approved") { success, msg ->
                                            if (success) {
                                                // Refresh data
                                                refreshUserData(userViewModel, userId)
                                            } else {
                                                Log.e("KYCVerification", "Failed to approve document: $msg")
                                            }
                                        }
                                    },
                                    onReject = {
                                        userViewModel.updateKYCStatus(userId, docTypeInfo.type, "rejected") { success, msg ->
                                            if (success) {
                                                // Refresh data
                                                refreshUserData(userViewModel, userId)
                                            } else {
                                                Log.e("KYCVerification", "Failed to reject document: $msg")
                                            }
                                        }
                                    },
                                    onViewFull = {
                                        // TODO: Implement full screen document viewer
                                    }
                                )
                            }
                        }
                    }
                }

                // Action buttons
                ActionButtonsSection(
                    hasDocuments = !kycDetails.isNullOrEmpty(),
                    allApproved = kycStats.first == 0 && kycStats.third == 0 && !kycDetails.isNullOrEmpty(),
                    onRejectClick = { showRejectionDialog = true },
                    onAcceptClick = { showApprovalDialog = true },
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
                        text = "Reject KYC Application",
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
                        OutlinedTextField(
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
                                focusedIndicatorColor = accentColor,
                                unfocusedIndicatorColor = textLightColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedLabelColor = accentColor,
                                unfocusedLabelColor = textLightColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = false,
                            maxLines = 5
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This reason will be shown to the user. The KYC documents will be removed and user will need to upload new documents.",
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
                                // Submit KYC rejection
                                userViewModel.verifyUserKYC(userId, false, rejectionReason) { success, msg ->
                                    if (success) {
                                        showRejectionDialog = false
                                        rejectionReason = ""
                                        activity?.finish() // Go back to admin dashboard
                                    } else {
                                        Log.e("KYCVerification", "Failed to reject KYC: $msg")
                                    }
                                }
                            },
                            enabled = rejectionReason.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = errorColor
                            )
                        ) {
                            Text("Reject KYC", color = textColor)
                        }
                    }
                },
                containerColor = primaryColor,
                titleContentColor = textColor,
                textContentColor = textLightColor
            )
        }

        // Approval Confirmation Dialog
        if (showApprovalDialog) {
            AlertDialog(
                onDismissRequest = { showApprovalDialog = false },
                title = {
                    Text(
                        text = "Approve KYC Application",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Are you sure you want to approve this KYC verification?",
                            color = textLightColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (kycStats.first > 0) {
                            Text(
                                text = "⚠️ Warning: ${kycStats.first} documents are still pending review.",
                                color = warningColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (kycStats.third > 0) {
                            Text(
                                text = "⚠️ Warning: ${kycStats.third} documents are rejected.",
                                color = warningColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = "Once approved, the user will gain full access to all platform features.",
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
                                showApprovalDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardBackgroundColor
                            )
                        ) {
                            Text("Cancel", color = textColor)
                        }
                        Button(
                            onClick = {
                                // Submit KYC approval
                                userViewModel.verifyUserKYC(userId, true, "") { success, msg ->
                                    if (success) {
                                        showApprovalDialog = false
                                        activity?.finish() // Go back to admin dashboard
                                    } else {
                                        Log.e("KYCVerification", "Failed to approve KYC: $msg")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = successColor
                            )
                        ) {
                            Text("Approve KYC", color = textColor)
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

// Helper data class for document types
private data class DocumentTypeInfo(
    val type: String,
    val displayName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun UserInfoSection(userData: UserModel) {
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
                    SubcomposeAsyncImage(
                        model = userData.profileImage,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
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
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
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
                        }
                    )
                }

                Column {
                    Text(
                        text = userData.fullName,
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${userData.userId.take(8)}...",
                        color = textLightColor,
                        fontSize = 14.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (userData.verified) successColor else pendingColor
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userData.verified) "Verified" else "Unverified",
                            color = if (userData.verified) successColor else pendingColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Additional user info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Email",
                        color = textLightColor,
                        fontSize = 12.sp
                    )
                    Text(
                        text = userData.email,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (userData.phoneNumber.isNotBlank()) {
                    Column {
                        Text(
                            text = "Phone",
                            color = textLightColor,
                            fontSize = 12.sp
                        )
                        Text(
                            text = userData.phoneNumber,
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Show rejection reason if exists
            if (!userData.kycRejectionReason.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = errorColor.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = errorColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Previous Rejection",
                                color = errorColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userData.kycRejectionReason,
                            color = textColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KYCStatusSummary(
    pending: Int,
    approved: Int,
    rejected: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatusItem(
                count = pending,
                label = "Pending",
                color = pendingColor
            )
            StatusItem(
                count = approved,
                label = "Approved",
                color = successColor
            )
            StatusItem(
                count = rejected,
                label = "Rejected",
                color = errorColor
            )
        }
    }
}

@Composable
private fun StatusItem(count: Int, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = textLightColor,
            fontSize = 12.sp
        )
    }
}

@Composable
fun KYCDocumentCard(
    documentName: String,
    documentType: String,
    kycStatus: KYCStatus,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onViewFull: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = documentName,
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatUploadTime(kycStatus.uploadedAt),
                            color = textLightColor,
                            fontSize = 12.sp
                        )
                    }
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (kycStatus.status.lowercase()) {
                                "approved" -> successColor
                                "rejected" -> errorColor
                                else -> pendingColor
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = kycStatus.status.replaceFirstChar { it.uppercaseChar() },
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Document preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                SubcomposeAsyncImage(
                    model = kycStatus.documentUrl,
                    contentDescription = documentName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onViewFull),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF3A3A3C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = "Document",
                                    modifier = Modifier.size(40.dp),
                                    tint = textLightColor
                                )
                                Text(
                                    text = "Loading document...",
                                    color = textLightColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF3A3A3C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Error",
                                    modifier = Modifier.size(40.dp),
                                    tint = textLightColor
                                )
                                Text(
                                    text = "Failed to load image",
                                    color = textLightColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                )

                // View full overlay
                if (kycStatus.documentUrl.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onViewFull),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            Text(
                                text = "View Full",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = textColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons (only show for pending documents)
            if (kycStatus.status.lowercase() == "pending") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = successColor)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Approve",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Approve")
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = errorColor)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Reject",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reject")
                        }
                    }
                }
            } else {
                // Show status message for non-pending documents
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        when (kycStatus.status.lowercase()) {
                            "approved" -> Icons.Default.CheckCircle
                            "rejected" -> Icons.Default.Close
                            else -> Icons.Default.Info
                        },
                        contentDescription = "Status",
                        tint = when (kycStatus.status.lowercase()) {
                            "approved" -> successColor
                            "rejected" -> errorColor
                            else -> pendingColor
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (kycStatus.status.lowercase()) {
                            "approved" -> "Document approved"
                            "rejected" -> "Document rejected"
                            else -> "Under review"
                        },
                        color = when (kycStatus.status.lowercase()) {
                            "approved" -> successColor
                            "rejected" -> errorColor
                            else -> pendingColor
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    hasDocuments: Boolean,
    allApproved: Boolean,
    onRejectClick: () -> Unit,
    onAcceptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!hasDocuments) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = warningColor.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = warningColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "No KYC Documents",
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "User hasn't uploaded any KYC documents yet",
                            color = textLightColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else if (!allApproved) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = warningColor.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = warningColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Review Required",
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Please review all documents before approving KYC",
                            color = textLightColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reject button
            Button(
                onClick = onRejectClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = errorColor
                ),
                enabled = hasDocuments
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
                ),
                enabled = hasDocuments && allApproved
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
                        text = "Approve KYC",
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// Helper functions
private fun formatUploadTime(timestamp: Long): String {
    if (timestamp == 0L) return "Recently"

    val currentTime = System.currentTimeMillis()
    val diff = currentTime - timestamp

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} min ago"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hours ago"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
        else -> {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

private fun convertOldKYCToNew(oldKycUrls: List<String>): Map<String, KYCStatus> {
    val kycTypes = listOf("citizenship_front", "citizenship_back", "pan", "bank", "profile")
    val newDetails = mutableMapOf<String, KYCStatus>()

    oldKycUrls.forEachIndexed { index, url ->
        if (index < kycTypes.size) {
            val docType = kycTypes[index]
            newDetails[docType] = KYCStatus(
                documentUrl = url,
                documentType = docType,
                status = "pending",
                uploadedAt = System.currentTimeMillis()
            )
        }
    }

    return newDetails
}

private fun refreshUserData(
    userViewModel: UserViewModel,
    userId: String
) {
    // For now, just reload the data, later update state
    userViewModel.getUserById(userId) { _, _, _ -> }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
fun KYCVerificationScreenPreview() {
    KYCVerificationScreen(userId = "test_user_123")
}