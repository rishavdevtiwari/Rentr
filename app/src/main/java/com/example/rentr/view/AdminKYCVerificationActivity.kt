package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.RentrTheme // Make sure this import matches your theme package
import com.example.rentr.viewmodel.AdminKYCViewModel // Import the Notification ViewModel
import com.example.rentr.viewmodel.UserViewModel

class AdminKYCVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("userId") ?: ""

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

    // 1. Existing Data ViewModel
    val userViewModel: UserViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return UserViewModel(UserRepoImpl()) as T
            }
        }
    )

    // 2. NEW Notification ViewModel
    val adminKYCViewModel: AdminKYCViewModel = viewModel()

    // State variables
    var userData by remember { mutableStateOf<UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showRejectionDialog by remember { mutableStateOf(false) }
    var showApprovalDialog by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf<String?>(null) }
    var verificationSuccess by remember { mutableStateOf(false) }

    // State for Rejection Reason input
    var rejectionReason by remember { mutableStateOf("") }

    // Fetch user data
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

                UserInfoSection(userData = userData!!)

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
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("No KYC documents found", color = textLightColor)
                            }
                        }
                    } else {
                        val documentTypes = listOf("Citizenship Front", "Citizenship Back", "PAN Card", "Bank Details", "Profile Photo")
                        items(userData!!.kycUrl.take(5).zip(documentTypes)) { (url, docName) ->
                            KYCDocumentCard(documentName = docName, documentUrl = url)
                        }
                    }
                }

                ActionButtonsSection(
                    hasDocuments = userData!!.kycUrl.isNotEmpty(),
                    isVerified = userData!!.verified,
                    onRejectClick = { showRejectionDialog = true },
                    onAcceptClick = { showApprovalDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        }

        // --- REJECTION DIALOG (Updated with Notification Logic) ---
        if (showRejectionDialog) {
            AlertDialog(
                onDismissRequest = { showRejectionDialog = false },
                title = { Text("Reject KYC Application", color = textColor, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Are you sure you want to reject this KYC verification?", color = textLightColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Added Text Field for Reason
                        OutlinedTextField(
                            value = rejectionReason,
                            onValueChange = { rejectionReason = it },
                            placeholder = { Text("Reason (e.g. Blurry photo)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rejectionReason.isNotBlank()) {
                                // 1. NOTIFICATION LOGIC
                                adminKYCViewModel.rejectKYC(userId, rejectionReason)

                                // 2. UPDATE UI LOCAL STATE
                                verificationSuccess = true
                                verificationMessage = "KYC Rejected & Notification Sent."
                                userData = userData?.copy(verified = false, kycUrl = emptyList())

                                showRejectionDialog = false
                                rejectionReason = "" // Reset
                            } else {
                                Toast.makeText(context, "Please enter a reason", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = errorColor)
                    ) {
                        Text("Reject KYC", color = textColor)
                    }
                },
                dismissButton = {
                    Button(onClick = { showRejectionDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor)) {
                        Text("Cancel", color = textColor)
                    }
                },
                containerColor = Color.Black
            )
        }

        // --- APPROVAL DIALOG (Updated with Notification Logic) ---
        if (showApprovalDialog) {
            AlertDialog(
                onDismissRequest = { showApprovalDialog = false },
                title = { Text("Verify KYC Application", color = textColor, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Are you sure you want to verify this KYC?", color = textLightColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("The user will be notified immediately.", color = textLightColor, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // 1. NOTIFICATION LOGIC
                            adminKYCViewModel.approveKYC(userId)

                            // 2. UPDATE UI LOCAL STATE
                            verificationSuccess = true
                            verificationMessage = "KYC Verified & Notification Sent!"
                            userData = userData?.copy(verified = true)

                            showApprovalDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = successColor)
                    ) {
                        Text("Verify KYC", color = textColor)
                    }
                },
                dismissButton = {
                    Button(onClick = { showApprovalDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor)) {
                        Text("Cancel", color = textColor)
                    }
                },
                containerColor = Color.Black
            )
        }
    }
}

// -------------------------------------------------------------------------
// HELPER COMPOSABLES (Exact copies of your original code)
// -------------------------------------------------------------------------

@Composable
private fun UserInfoSection(userData: UserModel) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(60.dp).clip(CircleShape)) {
                    SubcomposeAsyncImage(
                        model = userData.profileImage,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = { Box(Modifier.fillMaxSize().background(Color(0xFF3A3A3C))) }
                    )
                }
                Column {
                    Text(text = userData.fullName, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (userData.verified) successColor else accentColor))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (userData.verified) "Verified" else "Pending Review", color = if (userData.verified) successColor else accentColor, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Email", color = textLightColor, fontSize = 12.sp); Text(userData.email, color = textColor, fontSize = 14.sp) }
                if (userData.phoneNumber.isNotBlank()) {
                    Column { Text("Phone", color = textLightColor, fontSize = 12.sp); Text(userData.phoneNumber, color = textColor, fontSize = 14.sp) }
                }
            }
        }
    }
}

@Composable
fun KYCDocumentCard(documentName: String, documentUrl: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = documentName, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp))) {
                SubcomposeAsyncImage(
                    model = documentUrl,
                    contentDescription = documentName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = { Box(Modifier.fillMaxSize().background(Color(0xFF3A3A3C)), contentAlignment = Alignment.Center) { Text("Loading...", color = textLightColor) } }
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!hasDocuments) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = warningColor.copy(alpha = 0.2f))) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = warningColor)
                    Spacer(Modifier.width(12.dp))
                    Text("No KYC Documents uploaded", color = textColor)
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onRejectClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                    enabled = !isVerified
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reject")
                }
                Button(
                    onClick = onAcceptClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = successColor),
                    enabled = !isVerified
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Verify")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KYCVerificationScreenPreview() {
    RentrTheme {
        KYCVerificationScreen(userId = "test_user")
    }
}