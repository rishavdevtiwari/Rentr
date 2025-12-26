package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
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
import com.example.rentr.model.UserModel
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.viewmodel.UserViewModel

class AdminKYCVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ----------------------- INTENT CODE -----------------------
        val userId = intent.getStringExtra("userId") ?: ""
        // ----------------------- END INTENT CODE -----------------------

        setContent {
            RentrTheme {
                KYCVerificationScreen(userId = userId)
            }
        }
    }
}

// Color palette
private val accentColor = Color(0xFFFF6200)
private val successColor = Color(0xFF4CAF50)
private val errorColor = Color(0xFFF44336)
private val warningColor = Color(0xFFFFC107)
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
private val cardBackgroundColor = Color(0xFF2C2C2E)

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
    var showRejectionDialog by remember { mutableStateOf(false) }
    var showApprovalDialog by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf<String?>(null) }
    var verificationSuccess by remember { mutableStateOf(false) }

    // Fetch user data by userId
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId) { success, _, user ->
                if (success && user != null) {
                    userData = user
                } else {
                    Log.e("KYCVerification", "Failed to fetch user with ID: $userId")
                }
                isLoading = false
            }
        } else {
            isLoading = false
            Log.e("KYCVerification", "No userId provided")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "KYC Verification",
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
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
                    containerColor = Color.Black,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accentColor)
            }
        } else if (userData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "User not found",
                        tint = errorColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("User not found", color = textColor, fontSize = 18.sp)
                    Text("User ID: $userId", color = textLightColor, fontSize = 14.sp)
                    Button(
                        onClick = { activity?.finish() },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Show verification message if any
                verificationMessage?.let { message ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (verificationSuccess) successColor.copy(alpha = 0.2f)
                            else errorColor.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (verificationSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Message",
                                tint = if (verificationSuccess) successColor else errorColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = message,
                                color = textColor,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // User info section
                UserInfoSection(userData = userData!!)

                // KYC Documents Section
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (userData!!.kycUrl.isEmpty()) {
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
                        // Define document types
                        val documentTypes = listOf(
                            "Citizenship Front",
                            "Citizenship Back",
                            "PAN Card",
                            "Bank Details",
                            "Profile Photo"
                        )

                        items(userData!!.kycUrl.take(5).zip(documentTypes)) { (url, docName) ->
                            KYCDocumentCard(
                                documentName = docName,
                                documentUrl = url
                            )
                        }
                    }
                }

                // Action buttons - CHANGED: "Approve KYC" to "Verify KYC"
                ActionButtonsSection(
                    hasDocuments = userData!!.kycUrl.isNotEmpty(),
                    isVerified = userData!!.verified,
                    onRejectClick = { showRejectionDialog = true },
                    onAcceptClick = { showApprovalDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        // Rejection Confirmation Dialog
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
                            text = "Are you sure you want to reject this KYC verification?",
                            color = textLightColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The KYC documents will be removed and the user will need to upload new documents.",
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
                            onClick = { showRejectionDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardBackgroundColor
                            )
                        ) {
                            Text("Cancel", color = textColor)
                        }
                        Button(
                            onClick = {
                                showRejectionDialog = false
                                // Submit KYC rejection
                                userViewModel.verifyUserKYC(userId, false) { success, msg ->
                                    if (success) {
                                        verificationSuccess = true
                                        verificationMessage = "KYC rejected successfully. User needs to upload new documents."
                                        // Update local state
                                        userData = userData?.copy(
                                            verified = false,
                                            kycUrl = emptyList()
                                        )
                                    } else {
                                        verificationSuccess = false
                                        verificationMessage = "Failed to reject KYC: $msg"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = errorColor
                            )
                        ) {
                            Text("Reject KYC", color = textColor)
                        }
                    }
                },
                containerColor = Color.Black,
                titleContentColor = textColor,
                textContentColor = textLightColor
            )
        }

        // Approval Confirmation Dialog - CHANGED: "Approve KYC" to "Verify KYC"
        if (showApprovalDialog) {
            AlertDialog(
                onDismissRequest = { showApprovalDialog = false },
                title = {
                    Text(
                        text = "Verify KYC Application",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Are you sure you want to verify this KYC?",
                            color = textLightColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Once verified, the user will gain full access to all platform features.",
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
                            onClick = { showApprovalDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardBackgroundColor
                            )
                        ) {
                            Text("Cancel", color = textColor)
                        }
                        Button(
                            onClick = {
                                showApprovalDialog = false
                                // Submit KYC verification
                                userViewModel.verifyUserKYC(userId, true) { success, msg ->
                                    if (success) {
                                        verificationSuccess = true
                                        verificationMessage = "KYC verified successfully! User is now verified."
                                        // Update local state
                                        userData = userData?.copy(verified = true)
                                    } else {
                                        verificationSuccess = false
                                        verificationMessage = "Failed to verify KYC: $msg"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = successColor
                            )
                        ) {
                            Text("Verify KYC", color = textColor)
                        }
                    }
                },
                containerColor = Color.Black,
                titleContentColor = textColor,
                textContentColor = textLightColor
            )
        }
    }
}

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (userData.verified) successColor else accentColor
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userData.verified) "Verified" else "Pending Review",
                            color = if (userData.verified) successColor else accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User info
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

            Spacer(modifier = Modifier.height(8.dp))

            // KYC info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "KYC Status",
                        color = textLightColor,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (userData.kycUrl.isEmpty()) "No KYC"
                        else if (userData.verified) "Verified"
                        else "Pending",
                        color = if (userData.verified) successColor else accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                        text = "Documents",
                        color = textLightColor,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${userData.kycUrl.size}/5 uploaded",
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun KYCDocumentCard(
    documentName: String,
    documentUrl: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = documentName,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Document preview - REMOVED: View Full button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                SubcomposeAsyncImage(
                    model = documentUrl,
                    contentDescription = documentName,
                    modifier = Modifier.fillMaxSize(),
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
                                    Icons.Default.Description,
                                    contentDescription = "Document",
                                    modifier = Modifier.size(40.dp),
                                    tint = textLightColor
                                )
                                Text(
                                    text = "Loading...",
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
                                    text = "Failed to load",
                                    color = textLightColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    hasDocuments: Boolean,
    isVerified: Boolean,
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
        }

        if (isVerified) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = successColor.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = successColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Already Verified",
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "This user's KYC has already been approved",
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
                enabled = hasDocuments && !isVerified
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

            Button(
                onClick = onAcceptClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = successColor
                ),
                enabled = hasDocuments && !isVerified
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Verify", // Changed from Accept to Verify
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verify KYC", // CHANGED: "Approve KYC" to "Verify KYC"
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
fun KYCVerificationScreenPreview() {
    RentrTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            KYCVerificationScreen(userId = "test_user_123")
        }
    }
}