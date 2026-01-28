package com.example.rentr.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.promo
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) && (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val database = FirebaseDatabase.getInstance("https://rentr-db9e6-default-rtdb.firebaseio.com/")
                    database.getReference("users")
                        .child(currentUser.uid)
                        .child("fcmToken")
                        .setValue(token)
                }
            }
        }
        setContent {
            MainScreen()
        }
    }
}

data class Category(val name: String, val icon: ImageVector)

val categories = listOf(
    Category("Vehicles", Icons.Default.DirectionsCar),
    Category("Household", Icons.Default.Kitchen),
    Category("Electronics", Icons.Default.Laptop),
    Category("Accessories", Icons.Default.ShoppingCart),
    Category("Furniture", Icons.Default.Chair),
    Category("Sports & Adventure", Icons.Default.DirectionsBike),
    Category("Baby Items", Icons.Default.Toys)
)
private val textColor = Color.White
private val accentColor = Color(0xFFFF6200)
private val cardBackgroundColor = Color(0xFF2C2C2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val userViewModelDash = remember { UserViewModel(UserRepoImpl()) }
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val user by userViewModelDash.user.observeAsState(null)
    val products by productViewModel.allProducts.observeAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var selectedCategoryName by remember { mutableStateOf("All") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    fun refreshData() {
        coroutineScope.launch {
            isRefreshing = true
            userViewModelDash.getCurrentUser()?.uid?.let { userId ->
                userViewModelDash.getUserById(userId) { _, _, _ -> }
            }
            if (selectedCategoryName == "All") {
                productViewModel.getAllProducts { _, _, _ -> }
            } else {
                productViewModel.getAllProductsByCategory(selectedCategoryName) { _, _, _ -> }
            }
            delay(1000)
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        userViewModelDash.getCurrentUser()?.uid?.let { userId ->
            userViewModelDash.getUserById(userId) { _, _, _ -> }
        }
    }

    LaunchedEffect(selectedCategoryName) {
        if (selectedCategoryName == "All") {
            productViewModel.getAllProducts { _, _, _ -> }
        } else {
            productViewModel.getAllProductsByCategory(selectedCategoryName) { _, _, _ -> }
        }
    }

    val filteredProducts = products?.filter {
        it.verified && !it.flagged
    } ?: emptyList()

    val displayedProducts = filteredProducts.take(6)

    Scaffold(
        containerColor = Color.Black,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures { focusManager.clearFocus() }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { refreshData() },
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TopBar(userName = user?.fullName, userViewModelDash)
                    Spacer(modifier = Modifier.height(20.dp))
                    DashboardSearchBar()
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(promo, Color(0xFF1A1A1A)),
                                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                            end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Don't Buy, Just Rent!",
                                            color = Orange,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Experience more for less. Rent quality items near you.",
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp,
                                            lineHeight = 20.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Orange.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "Explore Now",
                                                color = Orange,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(80.dp))
                                }
                            }
                        }
                        Image(
                            painter = painterResource(R.drawable.rentrimage),
                            contentDescription = "Rentr Logo",
                            modifier = Modifier
                                .size(160.dp)
                                .align(Alignment.CenterEnd)
                                .offset(x = 20.dp, y = (-5).dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Categories", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Show All",
                            color = Orange.copy(0.7f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                val intent = Intent(context, CategoryActivity::class.java)
                                intent.putExtra("Category", selectedCategoryName)
                                context.startActivity(intent)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        CategorySearchChip("All", selectedCategoryName == "All") {
                            selectedCategoryName = "All"
                        }
                    }
                    items(categories) { category ->
                        CategorySearchChip(category.name, selectedCategoryName == category.name) {
                            selectedCategoryName = category.name
                        }
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(20.dp))
                    ProductGrid(products = displayedProducts)
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun TopBar(userName: String?, userViewModel: UserViewModel) {
    val user by userViewModel.user.observeAsState(null)
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        fun getInitials(name: String): String {
            val names = name.trim().split(" ")
            return if (names.size > 1) {
                "${names.first().firstOrNull() ?: ""}${names.last().firstOrNull() ?: ""}".uppercase()
            } else {
                (name.firstOrNull()?.toString() ?: "").uppercase()
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(37.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(accentColor, Color(0xFFFFC66C))))
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(cardBackgroundColor)
            ) {
                if (!user?.profileImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = user?.profileImage,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Text(
                        text = user?.fullName?.let { getInitials(it) } ?: "...",
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = userName ?: "...",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        IconButton(onClick = {
            val intent = Intent (context, NotificationActivity::class.java)
            context.startActivity(intent)
        }) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun DashboardSearchBar() {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, SearchActivity::class.java)
                context.startActivity(intent)
            }
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search for items...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = Field,
                disabledBorderColor = Color.Transparent,
                disabledPlaceholderColor = Color.Gray,
                disabledLeadingIconColor = Color.Gray
            )
        )
    }
}

@Composable
fun ProductGrid(products: List<ProductModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        products.chunked(2).forEach { rowProducts ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                rowProducts.forEach { product ->
                    Column(modifier = Modifier.weight(1f)) {
                        ProductCard(product)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(product.title, color = Orange, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "NPR. ${product.price}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                if (rowProducts.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ProductCard(product: ProductModel) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ProductActivity::class.java)
                intent.putExtra("productId", product.productId)
                intent.putExtra("listedBy",product.listedBy)
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field)
    ) {
        AsyncImage(
            model = product.imageUrl.firstOrNull(),
            placeholder = painterResource(id = R.drawable.rentrimage),
            error = painterResource(id = R.drawable.rentrimage),
            contentDescription = product.title,
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentScale = ContentScale.Crop
        )
    }
}
