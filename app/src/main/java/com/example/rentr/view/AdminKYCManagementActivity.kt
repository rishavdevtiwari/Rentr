package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.model.UserModel
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Orange
import com.example.rentr.viewmodel.UserViewModel

class AdminKYCManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                KYCListingScreen()
            }
        }
    }
}

enum class KYCStatus(val displayName: String, val color: Color) {
    PENDING("Pending Review", Orange),
    VERIFIED("Verified", Color(0xFF4CAF50)),
    REJECTED("Rejected", Color(0xFFF44336)),
    NO_KYC("No KYC", Color(0xFF9E9E9E))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KYCListingScreen() {
    val context = LocalContext.current

    // Initialize ViewModel
    val userViewModel: UserViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return UserViewModel(UserRepoImp1()) as T
            }
        }
    )

    // State for loading and error
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch users from Firebase
    val usersMap by userViewModel.allUsersMap.observeAsState(initial = emptyMap())
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        isLoading = true
        userViewModel.getAllUsers { success, message, _ ->
            isLoading = false
            if (!success) {
                errorMessage = message ?: "Failed to load users"
            }
        }
    }

    // Transform Map<userId, UserModel> to list for display
    val allUsers = remember(usersMap) {
        usersMap.map { (userId, user) ->
            UserKycItem(
                userId = userId,
                user = user,
                status = getKYCStatus(user)
            )
        }.sortedBy { it.user.fullName }
    }

    // Filter users based on selected tab
    val displayedUsers = remember(allUsers, selectedTab) {
        when (selectedTab) {
            1 -> allUsers.filter { it.status == KYCStatus.PENDING }
            2 -> allUsers.filter { it.status == KYCStatus.NO_KYC }
            else -> allUsers
        }
    }

    // Statistics
    val pendingCount = allUsers.count { it.status == KYCStatus.PENDING }
    val noKycCount = allUsers.count { it.status == KYCStatus.NO_KYC }
    val verifiedCount = allUsers.count { it.status == KYCStatus.VERIFIED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("KYC Verification", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Orange
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Error message
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x22FF0000)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Tabs
            KYCListingTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            // Statistics Row
            if (!isLoading && allUsers.isNotEmpty()) {
                StatisticsRow(
                    pendingCount = pendingCount,
                    verifiedCount = verifiedCount,
                    noKycCount = noKycCount
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            when {
                isLoading -> LoadingState()
                displayedUsers.isEmpty() -> EmptyState(selectedTab = selectedTab)
                else -> UsersList(displayedUsers = displayedUsers, context = context)
            }
        }
    }
}

private fun getKYCStatus(user: UserModel): KYCStatus {
    return when {
        user.verified -> KYCStatus.VERIFIED
        user.kycUrl.isNotEmpty() && !user.verified -> KYCStatus.PENDING
        else -> KYCStatus.NO_KYC
    }
}

@Composable
fun KYCListingTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("All Users", "Pending", "No KYC")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Orange else Color(0xFF2A2A2A)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 4.dp else 0.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticsRow(
    pendingCount: Int,
    verifiedCount: Int,
    noKycCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            count = pendingCount,
            label = "Pending",
            color = Orange,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            count = verifiedCount,
            label = "Verified",
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            count = noKycCount,
            label = "No KYC",
            color = Color(0xFF9E9E9E),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = Orange)
            Text(
                "Loading users...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun EmptyState(selectedTab: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.PersonSearch,
                contentDescription = "No users",
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Text(
                "No users found",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                when (selectedTab) {
                    1 -> "No pending KYC requests"
                    2 -> "All users have submitted KYC"
                    else -> "No users in database"
                },
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun UsersList(
    displayedUsers: List<UserKycItem>,
    context: android.content.Context
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(displayedUsers) { userItem ->
            SimpleKYCUserCard(
                userItem = userItem,
                onClick = {
                    // Only redirect if user has KYC documents AND status is PENDING
                    if (userItem.user.kycUrl.isNotEmpty() && userItem.status == KYCStatus.PENDING) {
                        val intent = Intent(context, AdminKYCVerificationActivity::class.java)
                        intent.putExtra("userId", userItem.userId)
                        context.startActivity(intent)
                    }
                }
            )
        }
    }
}

data class UserKycItem(
    val userId: String,
    val user: UserModel,
    val status: KYCStatus
)

@Composable
fun SimpleKYCUserCard(
    userItem: UserKycItem,
    onClick: () -> Unit
) {
    val user = userItem.user
    val status = userItem.status

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = user.kycUrl.isNotEmpty() && status == KYCStatus.PENDING,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Image
            Box(
                modifier = Modifier.size(60.dp)
            ) {
                AsyncImage(
                    model = user.profileImage,
                    contentDescription = user.fullName,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.rentrimage),
                    error = painterResource(id = R.drawable.rentrimage)
                )

                // Status indicator (small dot)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(status.color)
                        .border(
                            width = 2.dp,
                            color = Color(0xFF2A2A2A),
                            shape = CircleShape
                        )
                )
            }

            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name
                Text(
                    text = user.fullName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Email
                Text(
                    text = user.email,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status
                Box(
                    modifier = Modifier
                        .background(
                            status.color.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status.displayName,
                        color = status.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Chevron for clickable items
            if (user.kycUrl.isNotEmpty() && status == KYCStatus.PENDING) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Review KYC",
                    tint = Orange,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RentrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Orange,
            secondary = Orange,
            background = Color.Black,
            surface = Color(0xFF1E1E1E),
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

@Preview(showBackground = true, name = "KYC Listing Dark Mode")
@Composable
fun KYCListingScreenPreview() {
    RentrTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            KYCListingScreen()
        }
    }
}