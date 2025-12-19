package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.rentr.R
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminDashboardScreen()
        }
    }
}

// Data classes for admin dashboard
data class AdminProduct(
    val id: Int,
    val name: String,
    val listedBy: String,
    val price: Int,
    val imageRes: Int,
    val verificationStatus: VerificationStatus
)

data class UserKYC(
    val id: Int,
    val name: String,
    val imageRes: Int,
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

@Composable
fun AdminDashboardScreen() {
    // Mock data for demonstration
    val adminProducts = remember {
        (1..10).map { index ->
            AdminProduct(
                id = index,
                name = when (index % 4) {
                    0 -> "Mountain Bike Pro"
                    1 -> "DSLR Camera"
                    2 -> "Gaming Laptop"
                    else -> "Modern Sofa"
                },
                listedBy = "User ${index * 5}",
                price = (index * 1500) % 5000 + 500,
                imageRes = when (index % 3) {
                    0 -> R.drawable.bicycle
                    1 -> R.drawable.camera
                    else -> R.drawable.bike
                },
                verificationStatus = when (index % 3) {
                    0 -> VerificationStatus.VERIFIED
                    1 -> VerificationStatus.PENDING
                    else -> VerificationStatus.REJECTED
                }
            )
        }
    }

    val kycUsers = remember {
        (1..8).map { index ->
            UserKYC(
                id = index,
                name = "John Doe $index",
                imageRes = when (index % 3) {
                    0 -> R.drawable.p1
                    1 -> R.drawable.bicycle
                    else -> R.drawable.camera
                },
                verificationStatus = when (index % 3) {
                    0 -> VerificationStatus.VERIFIED
                    1 -> VerificationStatus.PENDING
                    else -> VerificationStatus.REJECTED
                }
            )
        }
    }

    val flaggedProducts = remember {
        (1..10).map { index ->
            FlaggedProduct(
                id = index,
                productName = "Product $index",
                productImageRes = when (index % 3) {
                    0 -> R.drawable.bike
                    1 -> R.drawable.car
                    else -> R.drawable.toy
                },
                flaggedBy = "Reporter ${index * 3}",
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

    Scaffold(
        containerColor = Color.Black,
        topBar = { AdminTopBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Section 1: Products
                SectionHeader(
                    title = "Product Management",
                    subtitle = "Manage all listed products",
                    icon = Icons.Default.Info
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProductListSection(products = adminProducts)

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: KYC Verification
                SectionHeader(
                    title = "KYC Verification",
                    subtitle = "Review user verification requests",
                    icon = Icons.Default.Person
                )
                Spacer(modifier = Modifier.height(12.dp))
                KYCListSection(users = kycUsers)

                Spacer(modifier = Modifier.height(24.dp))

                // Section 3: Flagged Products
                SectionHeader(
                    title = "Flagged Items",
                    subtitle = "Review flagged products",
                    icon = Icons.Default.Warning
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlaggedProductsSection(products = flaggedProducts)

                Spacer(modifier = Modifier.height(100.dp)) // Extra space for scrolling
            }
        }
    }
}

@Composable
fun AdminTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Admin Dashboard", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Manage platform content", color = Orange.copy(alpha = 0.8f), fontSize = 14.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Admin",
                tint = Orange,
                modifier = Modifier
                    .size(40.dp)
                    .background(Orange.copy(alpha = 0.2f), CircleShape)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
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
        Text(
            "View All",
            color = Orange,
            fontSize = 14.sp,
            modifier = Modifier.clickable { /* Handle view all */ }
        )
    }
}

@Composable
fun ProductListSection(products: List<AdminProduct>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(products) { product ->
            AdminProductCard(product = product)
        }
    }
}

@Composable
fun AdminProductCard(product: AdminProduct) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { /* Handle product click */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Product Image
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.name,
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
                    fontSize = 12.sp
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
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(users) { user ->
            KYCUserCard(user = user)
        }
    }
}

@Composable
fun KYCUserCard(user: UserKYC) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { /* Handle user click */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Profile Image
            Box {
                Image(
                    painter = painterResource(id = user.imageRes),
                    contentDescription = user.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                // Verification Status Indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .background(Color.Black, CircleShape)
                ) {
                    Icon(
                        when (user.verificationStatus) {
                            VerificationStatus.VERIFIED -> Icons.Default.CheckCircle
                            VerificationStatus.PENDING -> Icons.Default.Info
                            VerificationStatus.REJECTED -> Icons.Default.Close
                        },
                        contentDescription = "Status",
                        tint = user.verificationStatus.color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                user.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .background(
                        user.verificationStatus.color.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    user.verificationStatus.text,
                    color = user.verificationStatus.color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = { /* Approve KYC */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Approve",
                        tint = Color.Green
                    )
                }
                IconButton(
                    onClick = { /* Reject KYC */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Reject",
                        tint = Color.Red
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
            // Product Image with Flag Indicator
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
                // Flag Type Badge
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

            // Product Info
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
                    painter=painterResource(id=R.drawable.baseline_priority_high_24),
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
                // Resolved Status
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

                // Action Button
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
                    VerificationStatus.PENDING -> Icons.Default.Info
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

@Composable
fun IconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview
@Composable
fun AdminDashboardPreview() {
    AdminDashboardScreen()
}