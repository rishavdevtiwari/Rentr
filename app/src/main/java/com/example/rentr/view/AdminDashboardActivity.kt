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
import androidx.compose.foundation.rememberScrollState
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
import com.example.rentr.model.ProductModel
import com.example.rentr.model.UserModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel
import kotlin.collections.filter

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                AdminDashboardScreen()
            }
        }
    }
}

// Data classes for admin dashboard
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

//data class FlaggedProduct(
//    val id: Int,
//    val productName: String,
//    val productImageRes: Int,
//    val flaggedBy: String,
//    val flagType: FlagType,
//    val isResolved: Boolean
//)

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
    val activity = context as? Activity
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
                listedBy = sellerName, // Use full name instead of ID
                price = product.price.toInt(),
                imageRes = product.imageUrl.firstOrNull() ?: "",
                verificationStatus = if (product.verified) VerificationStatus.VERIFIED else VerificationStatus.PENDING
            )
        }
    }

    // Get users with pending KYC
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

    // Demo flagged products
//    val flaggedProducts = remember {
//        (1..5).map { index ->
//            FlaggedProduct(
//                id = index,
//                productName = "Product $index",
//                productImageRes = when (index % 3) {
//                    0 -> R.drawable.bike
//                    1 -> R.drawable.car
//                    else -> R.drawable.toy
//                },
//                flaggedBy = "User ${index * 3}",
//                flagType = when (index % 4) {
//                    0 -> FlagType.SUSPICIOUS
//                    1 -> FlagType.INAPPROPRIATE
//                    2 -> FlagType.DUPLICATE
//                    else -> FlagType.OUTDATED
//                },
//                isResolved = index % 3 == 0
//            )
//        }
//    }

    Scaffold(
        topBar = { AdminTopBar() },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
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
                        // Navigate to Product Verification Activity
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
                    subtitle = "${products.count { it.flagged && it.flaggedBy.isNotEmpty() }} pending review",
                    icon = Icons.Default.Warning,
                    onViewAllClick = {
                        // Navigate to AdminReviewManagementActivity
                        val intent = Intent(context, AdminReviewManagementActivity::class.java)
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                FlaggedProductsSection(
                    products = products,
                    usersMap = usersMap
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                // Verification Status Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    VerificationBadge(status = product.verificationStatus)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Info
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
            // User Profile Image with Status
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Profile Image
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

                // Status indicator (small ring)
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

            // User Name (centered)
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

            // Status Badge (centered)
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
fun FlaggedProductsSection(
    products: List<ProductModel>,
    usersMap: Map<String, UserModel>
) {
    val context = LocalContext.current

    val flaggedProducts = remember(products) {
        products.filter {
            it.flagged && it.flaggedBy.isNotEmpty()
        }.take(5)
    }

    if (flaggedProducts.isEmpty()) {
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
                    Icons.Default.CheckCircle,
                    contentDescription = "No flagged items",
                    tint = Color.Green,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "No flagged items",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "All items are clean and verified",
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
            items(flaggedProducts) { product ->
                FlaggedProductCard(
                    product = product,
                    sellerName = usersMap[product.listedBy]?.fullName ?: "Unknown",
                    onClick = {
                        // Navigate to Flag Review Activity
                        val intent = Intent(context, AdminFlaggedReviewActivity::class.java)
                        intent.putExtra("productId", product.productId)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun FlaggedProductCard(
    product: ProductModel,
    sellerName: String,
    onClick: () -> Unit
) {
    val flagStatus = when {
        product.appealReason.isNotEmpty() -> "APPEAL"
        product.flagged -> "FLAGGED"
        else -> "RESOLVED"
    }

    val flagStatusColor = when (flagStatus) {
        "APPEAL" -> Color.Cyan
        "FLAGGED" -> Color.Yellow
        else -> Color.Green
    }

    val flagReasonText = if (product.flaggedReason.isNotEmpty()) {
        // Take the first flag reason (or combine them)
        val firstReason = product.flaggedReason.firstOrNull() ?: ""
        if (product.flaggedReason.size > 1) {
            "$firstReason (+${product.flaggedReason.size - 1} more)"
        } else {
            firstReason
        }
    } else {
        "No reason provided"
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Product Image
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = product.imageUrl.firstOrNull() ?: "",
                    contentDescription = product.title,
                    placeholder = painterResource(id = R.drawable.rentrimage),
                    error = painterResource(id = R.drawable.rentrimage),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Flag Status Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            flagStatusColor,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        flagStatus,
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Info
            Text(
                product.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Seller Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Listed by",
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    sellerName,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Flag Reason (shortened)
            if (product.flaggedReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    flagReasonText.take(30) + if (flagReasonText.length > 30) "..." else "",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flag Count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = "Flag count",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${product.flaggedBy.size}",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Price
                Text(
                    "NPR. ${product.price.toInt()}",
                    color = Orange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Review Button
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Review",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
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
        AdminDashboardScreen()
    }
}