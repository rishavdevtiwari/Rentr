package com.example.rentr.viewmodel



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.rentr.ui.theme.Button
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.outline
import com.example.rentr.ui.theme.red
import com.example.rentr.ui.theme.splash

// --- 1. CONFIGURATION & THEME ---
class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = splash,
                    background = splash,
                    surface = splash
                )
            ) {
                // We use Surface to fill the screen with the background color
                Surface(modifier = Modifier.fillMaxSize(), color = splash) {
                    RentrProductVerify()
                }
            }
        }
    }
}

// --- 2. DATA MODELS & ENUMS ---
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val status: ProductStatus,
    val isVerified: Boolean
)

enum class ProductStatus(val label: String, val color: Color) {
    Active("Active", Color(0xFF4CAF50)),     // Green
    Scheduled("Scheduled", Color(0xFF2196F3)), // Blue
    Available("Available", red)
}

enum class AdminBottomNavItem(val label: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Outlined.Home),
    Product("Product", Icons.Outlined.ShoppingBag),
    KYC("KYC", Icons.Outlined.VerifiedUser),
    Review("Review", Icons.Outlined.RateReview),
    Settings("Settings", Icons.Outlined.Settings)
}

// --- 3. MAIN SCREEN (MOBILE ONLY) ---
@Composable
fun RentrProductVerify() {
    var currentScreen by remember { mutableStateOf(AdminBottomNavItem.Product) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = splash
            ) {
                AdminBottomNavItem.values().forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentScreen == item,
                        onClick = { currentScreen = item },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Orange,
                            selectedTextColor = Orange,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (currentScreen) {
                AdminBottomNavItem.Dashboard -> PlaceholderScreen("Dashboard")
                AdminBottomNavItem.Product -> ProductScreen()
                AdminBottomNavItem.KYC -> PlaceholderScreen("KYC Verification")
                AdminBottomNavItem.Review -> PlaceholderScreen("Review")
                AdminBottomNavItem.Settings -> PlaceholderScreen("Settings")
            }
        }
    }
}

@Composable
fun ProductScreen() {
    val allProducts = remember {
        mutableStateListOf(
            Product("1", "T-Shirt", "Women Cloths", 79.80, 79, ProductStatus.Scheduled, true),
            Product("2", "Shirt", "Man Cloths", 76.89, 86, ProductStatus.Active, true),
            Product("3", "Pant", "Kid Cloths", 86.65, 74, ProductStatus.Available, false),
            Product("4", "Sweater", "Man Cloths", 56.07, 69, ProductStatus.Active, true),
            Product("5", "Light Jacket", "Women Cloths", 36.00, 65, ProductStatus.Available, false),
            Product("6", "Sneakers", "Footwear", 120.50, 40, ProductStatus.Active, true),
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    val displayedProducts = when (selectedTab) {
        1 -> allProducts.filter { it.isVerified }
        2 -> allProducts.filter { !it.isVerified }
        else -> allProducts
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header (Menu, Title, Search, Profile)
        DashboardHeader()

        Spacer(modifier = Modifier.height(24.dp))

        // "Products List" Label and "Add" Button
        ListControls(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        Spacer(modifier = Modifier.height(16.dp))

        // The Scrollable List of Cards
        ProductList(
            products = displayedProducts,
            onVerifyClick = { product ->
                val index = allProducts.indexOfFirst { it.id == product.id }
                if (index != -1) {
                    allProducts[index] = allProducts[index].copy(isVerified = true)
                }
            }
        )
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
    }
}

// --- 4. COMPONENT PARTS ---

@Composable
fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: App Name (Menu Icon Removed)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Rentr",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Orange
            )
        }

        // Right: Search + Profile
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* Search Action */ }) {
                Icon(Icons.Outlined.Search, contentDescription = "Search")
            }
            // Profile Placeholder
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.LightGray, RoundedCornerShape(18.dp))
            )
        }
    }
}

@Composable
fun ListControls(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tabs = listOf("All Products", "Verified", "Unverified")
        tabs.forEachIndexed { index, title ->
            Column(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .clickable { onTabSelected(index) }
            ) {
                Text(
                    text = title,
                    fontSize = if (selectedTab == index) 20.sp else 16.sp,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedTab == index) Color.Black else Color.Gray
                )
                if (selectedTab == index) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(2.dp)
                            .width(20.dp) // Little indicator
                            .background(Orange, RoundedCornerShape(2.dp))
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductList(products: List<Product>, onVerifyClick: (Product) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(products) { product ->
            ProductCard(product, onVerifyClick)
        }
    }
}

@Composable
fun ProductCard(product: Product, onVerifyClick: (Product) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Top Row: Image, Name/Cat, Status
            Row(verticalAlignment = Alignment.Top) {
                // Image Placeholder
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Name and Category
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = product.category,
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (product.isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = if (product.isVerified) "Verified" else "Unverified",
                            tint = if (product.isVerified) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (product.isVerified) "Verified" else "Unverified",
                            fontSize = 12.sp,
                            color = if (product.isVerified) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }
                }

                // Status Badge (Top Right)
                StatusBadge(status = product.status)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Stock and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = "Stock",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${product.stock} in stock",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "$${product.price}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Button
                )
            }

            if (!product.isVerified) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onVerifyClick(product) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) {
                    Text("Verify Product")
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ProductStatus) {
    Surface(
        color = status.color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = status.label,
            color = status.color,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

// --- 5. PREVIEW ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MobilePreview() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = splash,
            background = splash,
            surface = splash
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = splash) {
            RentrProductVerify()
        }
    }
}
