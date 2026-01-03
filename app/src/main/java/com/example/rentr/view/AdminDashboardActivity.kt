
package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.model.ProductModel
import com.example.rentr.model.UserModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel

// --- NAVIGATION CONFIGURATION ---

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object KYC : BottomNavItem("kyc", "KYC", Icons.Default.VerifiedUser)
    object Product : BottomNavItem("product", "Product", Icons.Default.ShoppingCart)
    object Review : BottomNavItem("review", "Review", Icons.Default.RateReview)
    object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
}

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                AdminMainParent()
            }
        }
    }
}

// --- MAIN PARENT COMPOSABLE ---

@Composable
fun AdminMainParent() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AdminBottomNavBar(navController = navController) },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = BottomNavItem.Home.route) {

                // 1. HOME TAB
                composable(BottomNavItem.Home.route) {
                    AdminDashboardScreen(
                        // Pass navigation callbacks to switch tabs instead of starting Activities
                        onNavigateToProduct = {
                            navController.navigate(BottomNavItem.Product.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToKYC = {
                            navController.navigate(BottomNavItem.KYC.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToReview = {
                            navController.navigate(BottomNavItem.Review.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                // 2. KYC TAB
                composable(BottomNavItem.KYC.route) {
                    PlaceholdersScreen("KYC Management")
                }

                // 3. PRODUCT TAB
                composable(BottomNavItem.Product.route) {
                    PlaceholdersScreen("Product Management")
                }

                // 4. REVIEW TAB
                composable(BottomNavItem.Review.route) {
                    PlaceholdersScreen("Flagged Reviews")
                }

                // 5. SETTINGS TAB
                composable(BottomNavItem.Settings.route) {
                    PlaceholdersScreen("Settings")
                }
            }
        }
    }
}

@Composable
fun AdminBottomNavBar(navController: NavController) {
    // 1. Get Context for Intents
    val context = LocalContext.current

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.KYC,
        BottomNavItem.Product,
        BottomNavItem.Review,
        BottomNavItem.Settings
    )

    Surface(
        color = Color.Black,
        contentColor = Orange,
        tonalElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Black,
            contentColor = Orange
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { item ->
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(text = item.title, fontSize = 10.sp) },
                    selected = currentRoute == item.route,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Orange,
                        indicatorColor = Orange,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    onClick = {
                        // 2. Logic to switch screens
                        when (item) {
                            BottomNavItem.Home -> {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            BottomNavItem.KYC -> {
                                val intent = Intent(context, AdminKYCManagementActivity::class.java)
                                context.startActivity(intent)
                            }
                            BottomNavItem.Product -> {
                                val intent = Intent(context, AdminProductManagementActivity::class.java)
                                context.startActivity(intent)
                            }
                            BottomNavItem.Review -> {
                                val intent = Intent(context, AdminReviewManagementActivity::class.java)
                                context.startActivity(intent)
                            }
                            BottomNavItem.Settings -> {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholdersScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Orange,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Coming Soon",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

// --- DATA CLASSES ---

data class AdminProduct(
    val id: String,
    val name: String,
    val listedBy: String,
    val price: Int,
    val imageRes: String,
    val verificationStatus: VerificationStatus
)

data class UserKYC(
    val userId: String,
    val name: String,
    val imageRes: String,
    val verificationStatus: VerificationStatus
)

enum class VerificationStatus(val color: Color, val text: String) {
    VERIFIED(Orange, "Verified"),
    PENDING(Color.Yellow, "Pending"),
    REJECTED(Color.Red, "Rejected")
}

@Composable
fun VerificationBadge(status: VerificationStatus) {
    Box(
        modifier = Modifier
            .background(status.color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.text.uppercase(),
            color = status.color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Orange,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
        TextButton(onClick = onViewAllClick) {
            Text("View All", color = Orange)
        }
    }
}

// --- DASHBOARD LOGIC ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    productViewModel: ProductViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProductViewModel(ProductRepoImpl()) as T
            }
        }
    ),
    userViewModel: UserViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return UserViewModel(UserRepoImp1()) as T
            }
        }
    ),
    // Added callbacks for navigation
    onNavigateToProduct: () -> Unit,
    onNavigateToKYC: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    val context = LocalContext.current
    val products by productViewModel.allProducts.observeAsState(initial = emptyList())
    val usersMap by userViewModel.allUsersMap.observeAsState(initial = emptyMap())

    LaunchedEffect(Unit) {
        productViewModel.getAllProducts { _, _, _ -> }
        userViewModel.getAllUsers { _, _, _ -> }
    }

    val adminProducts = remember(products, usersMap) {
        products.map { product ->
            val sellerName = usersMap[product.listedBy]?.fullName ?: "Unknown"
            AdminProduct(
                id = product.productId,
                name = product.title,
                listedBy = sellerName,
                price = product.price.toInt(),
                imageRes = product.imageUrl.firstOrNull() ?: "",
                verificationStatus = if (product.verified) VerificationStatus.VERIFIED else VerificationStatus.PENDING
            )
        }
    }

    val pendingKYCUsers = remember(usersMap) {
        usersMap.filter { (_, user) ->
            user.kycUrl.isNotEmpty() && !user.verified
        }.entries.map { (userId, user) ->
            UserKYC(
                userId = userId,
                name = user.fullName,
                imageRes = user.profileImage,
                verificationStatus = VerificationStatus.PENDING
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AdminTopBar()

        Column(modifier = Modifier.padding(16.dp)) {
            // Section 1: Products Management
            SectionHeader(
                title = "Product Management",
                subtitle = "Manage all listed products",
                icon = Icons.Default.Info,
                onViewAllClick = onNavigateToProduct // Use callback
            )
            Spacer(modifier = Modifier.height(12.dp))

            ProductListSection(
                products = adminProducts.take(3),
                onProductClick = { product ->
                    val intent = Intent(context, AdminProductVerificationActivity::class.java).apply {
                        putExtra("productId", product.id)
                        putExtra("productName", product.name)
                        putExtra("listedBy", product.listedBy)
                        putExtra("price", product.price)
                        putExtra("imageUrl", product.imageRes)
                        putExtra("verificationStatus", product.verificationStatus.name)
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: KYC Verification
            SectionHeader(
                title = "KYC Verification",
                subtitle = "Verify user identities",
                icon = Icons.Default.VerifiedUser,
                onViewAllClick = onNavigateToKYC
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (pendingKYCUsers.isNotEmpty()) {
                UserKycListSection(
                    users = pendingKYCUsers.take(3),
                    onUserClick = { user ->
                        // TODO: Navigate to user KYC detail screen
                    }
                )
            } else {
                Text(
                    "No pending KYC requests.",
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            // Section 3: Flagged Reviews
            SectionHeader(
                title = "Flagged Reviews",
                subtitle = "Moderate user-submitted reviews",
                icon = Icons.Default.Flag,
                onViewAllClick = onNavigateToReview // Use callback
            )
            // TODO: Add content for Flagged Reviews

        }
    }
}

@Composable
fun AdminTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Welcome Back,", color = Color.Gray)
            Text("Admin", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background), // Replace with actual admin image
            contentDescription = "Admin Profile",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(2.dp, Orange, CircleShape)
        )
    }
}

@Composable
fun ProductListSection(
    products: List<AdminProduct>,
    onProductClick: (AdminProduct) -> Unit
) {
    if (products.isEmpty()) {
        Text(
            "No products to display.",
            color = Color.Gray,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp)
        )
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                AdminProductItem(product, onClick = { onProductClick(product) })
            }
        }
    }
}

@Composable
fun UserKycListSection(
    users: List<UserKYC>,
    onUserClick: (UserKYC) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(users) { user ->
            AdminUserKycItem(user, onClick = { onUserClick(user) })
        }
    }
}


@Composable
fun AdminProductItem(product: AdminProduct, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Field)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    model = product.imageRes,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    VerificationBadge(status = product.verificationStatus)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text("by ${product.listedBy}", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Text("₹${product.price}/day", color = Orange, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun AdminUserKycItem(user: UserKYC, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AsyncImage(
                model = user.imageRes,
                contentDescription = user.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, user.verificationStatus.color, CircleShape),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "KYC Status",
                    tint = user.verificationStatus.color,
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Black, CircleShape)
                        .padding(4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = user.name,
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1,
            fontWeight = FontWeight.SemiBold
        )
    }
}

