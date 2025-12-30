package com.example.rentr.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel

// --- NAVIGATION ITEMS ---
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
                // We call the Parent Wrapper here, not the Dashboard directly
                AdminMainParent()
            }
        }
    }
}

// --- MAIN PARENT WRAPPER (Handles Navigation) ---
@Composable
fun AdminMainParent() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AdminBottomNavBar(navController = navController) },
        containerColor = Color.Black
    ) { innerPadding ->
        // We wrap the content in a Box to apply the padding from the bottom bar
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = BottomNavItem.Home.route) {

                // 1. HOME TAB: Your Original Dashboard
                composable(BottomNavItem.Home.route) {
                    AdminDashboardScreen()
                }

                // 2. KYC TAB
                composable(BottomNavItem.KYC.route) {
                    PlaceholdersScreen("KYC Management")
                }

                // 3. PRODUCT TAB
                composable(BottomNavItem.Product.route) {
                    PlaceholdersScreen("Product Management")
                }

                //5. REVIEW TAB
                composable(BottomNavItem.Review.route) {
                    PlaceholdersScreen("Review")
                }


                // 4. SETTINGS TAB
                composable(BottomNavItem.Settings.route) {
                    PlaceholdersScreen("Settings")
                }
            }
        }
    }
}

@Composable
fun AdminBottomNavBar(navController: NavController) {
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
                    label = { Text(text = item.title) },
                    selected = currentRoute == item.route,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Orange,
                        indicatorColor = Orange,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
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
        Text(
            text = title,
            color = Orange,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- YOUR ORIGINAL DATA CLASSES ---
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

data class FlaggedProduct(
    val id: Int,
    val productName: String,
    val productImageRes: Int,
    val flaggedBy: String,
    val flagType: FlagType,
    val isResolved: Boolean
)

enum class VerificationStatus(val color: Color, val text: String) {
    VERIFIED(Orange, "Verified"),
    PENDING(Color.Yellow, "Pending"),
    REJECTED(Color.Red, "Rejected")
}

enum class FlagType(val color: Color, val text: String) {
    SUSPICIOUS(Color.Yellow, "Suspicious"),
    INAPPROPRIATE(Color.Red, "Inappropriate"),
    DUPLICATE(Color.Cyan, "Duplicate"),
    OUTDATED(Color.Gray, "Outdated")
}


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
    )
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

    val flaggedProducts = remember {
        (1..5).map { index ->
            FlaggedProduct(
                id = index,
                productName = "Product $index",
                productImageRes = when (index % 3) {
                    0 -> R.drawable.bike
                    1 -> R.drawable.car
                    else -> R.drawable.toy
                },
                flaggedBy = "User ${index * 3}",
                flagType = when (index % 4) {
                    0 -> FlagType.SUSPICIOUS
                    1 -> FlagType.INAPPROPRIATE
                    2 -> FlagType.DUPLICATE
                    else -> FlagType.OUTDATED
                },
                isResolved = index % 3 == 0
            )
        }
    }

    // NOTE: Removed Scaffold here because it's now handled in AdminMainParent.
    // We just keep the column content.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AdminTopBar() // Kept Top Bar here

        Column(modifier = Modifier.padding(16.dp)) {
            // Section 1: Products Management
            SectionHeader(
                title = "Product Management",
                subtitle = "Manage all listed products",
                icon = Icons.Default.Info,
                onViewAllClick = {
                    val intent = Intent(context, AdminProductManagementActivity::class.java)
                    context.startActivity(intent)
                }
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
                subtitle = "${pendingKYCUsers.size} pending requests",
                icon = Icons.Default.Person,
                onViewAllClick = {
                    val intent = Intent(context, AdminKYCManagementActivity::class.java)
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            KYCListSection(users = pendingKYCUsers)

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Flagged Products
            SectionHeader(
                title = "Flagged Items",
                subtitle = "Review flagged products",
                icon = Icons.Default.Warning,
                onViewAllClick = {
                    Toast.makeText(context, "Flagged Items Management not added", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlaggedProductsSection(products = flaggedProducts)

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar() {
    TopAppBar(
        title = {
            Column {
                Text("Admin Dashboard", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Manage platform content", color = Orange.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black,
            titleContentColor = Color.White
        ),
        actions = {
            IconButton(onClick = { /* Profile or settings */ }) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Admin",
                    tint = Orange,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Orange.copy(alpha = 0.2f), CircleShape)
                        .padding(6.dp)
                )
            }
        }
    )
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = Orange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
        TextButton(
            onClick = onViewAllClick
        ) {
            Text(
                text = "View All",
                color = Orange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ProductListSection(
    products: List<AdminProduct>,
    onProductClick: (AdminProduct) -> Unit = {}
) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No products found",
                color = Color.Gray,
            )
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(products) { product ->
                AdminProductCard(
                    product = product,
                    onProductClick = onProductClick
                )
            }
        }
    }
}

@Composable
fun AdminProductCard(
    product: AdminProduct,
    onProductClick: (AdminProduct) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onProductClick(product) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Product Image
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = product.imageRes,
                    contentDescription = product.name,
                    placeholder = painterResource(id = R.drawable.rentrimage),
                    error = painterResource(id = R.drawable.rentrimage),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    VerificationBadge(status = product.verificationStatus)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                product.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Listed by",
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    product.listedBy,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "NPR. ${product.price}",
                    color = Orange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    product.verificationStatus.text,
                    color = product.verificationStatus.color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun KYCListSection(users: List<UserKYC>) {
    val context = LocalContext.current

    if (users.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Verified,
                    contentDescription = "No pending KYC",
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "No pending KYC requests",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "All KYC requests have been reviewed",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(users) { user ->
                KYCUserCard(
                    user = user,
                    onUserClick = { userId ->
                        val intent = Intent(context, AdminKYCVerificationActivity::class.java)
                        intent.putExtra("userId", userId)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun KYCUserCard(
    user: UserKYC,
    onUserClick: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onUserClick(user.userId) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = user.imageRes,
                    contentDescription = user.name,
                    placeholder = painterResource(id = R.drawable.rentrimage),
                    error = painterResource(id = R.drawable.rentrimage),
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .border(
                            width = 2.dp,
                            color = user.verificationStatus.color,
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = user.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(
                        user.verificationStatus.color.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        when (user.verificationStatus) {
                            VerificationStatus.VERIFIED -> Icons.Default.CheckCircle
                            VerificationStatus.PENDING -> Icons.Default.Schedule
                            VerificationStatus.REJECTED -> Icons.Default.Close
                        },
                        contentDescription = "Status",
                        tint = user.verificationStatus.color,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = user.verificationStatus.text,
                        color = user.verificationStatus.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FlaggedProductsSection(products: List<FlaggedProduct>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(products) { product ->
            FlaggedProductCard(product = product)
        }
    }
}

@Composable
fun FlaggedProductCard(product: FlaggedProduct) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { /* Handle flagged product click */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = product.productImageRes),
                    contentDescription = product.productName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(product.flagType.color, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        product.flagType.text,
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                product.productName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PriorityHigh,
                    contentDescription = "Flagged by",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "By: ${product.flaggedBy}",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (product.isResolved) Color.Green else Color.Red,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (product.isResolved) "Resolved" else "Pending",
                        color = if (product.isResolved) Color.Green else Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (product.isResolved) Color.Gray.copy(alpha = 0.3f) else Orange)
                        .clickable(enabled = !product.isResolved) { /* Handle action */ }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (product.isResolved) "View" else "Review",
                        color = if (product.isResolved) Color.Gray else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationBadge(status: VerificationStatus) {
    Box(
        modifier = Modifier
            .background(status.color, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                when (status) {
                    VerificationStatus.VERIFIED -> Icons.Default.Verified
                    VerificationStatus.PENDING -> Icons.Default.Schedule
                    VerificationStatus.REJECTED -> Icons.Default.Close
                },
                contentDescription = status.text,
                tint = Color.Black,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                status.text,
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun AdminDashboardPreview() {
    RentrTheme {
        AdminMainParent()
    }
}