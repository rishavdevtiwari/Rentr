package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.promo
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DashboardScreen()
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

@Composable
fun DashboardScreen() {
    val userViewModelDash = remember { UserViewModel(UserRepoImp1()) }
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val user by userViewModelDash.user.observeAsState(null)
    val products by productViewModel.allProducts.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        // Fetch current user
        userViewModelDash.getCurrentUser()?.uid?.let { userId ->
            userViewModelDash.getUserById(userId) { _, _, _ -> }
        }

        // Fetch all products initially
        productViewModel.getAllProducts { _, _, _ -> }
    }

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // When a category is selected, fetch products for that category
    LaunchedEffect(selectedCategory) {
        if (selectedCategory != null) {
            productViewModel.getAllProductsByCategory(selectedCategory!!.name) { _, _, _ -> }
        } else {
            // If no category selected, show all products
            productViewModel.getAllProducts { _, _, _ -> }
        }
    }

    // Filter products based on search and verification/flag status
    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        if (selectedCategory != null) {
            // Show products from selected category
            products.filter {
                it.verified && !it.flagged && it.category == selectedCategory!!.name &&
                        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
            }
        } else {
            // Show all products (filtered)
            products.filter {
                it.verified && !it.flagged &&
                        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
            }
        }
    }

    // For dashboard preview - limit to 6 items
    val displayedProducts = remember(filteredProducts) {
        filteredProducts.take(6)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures { focusManager.clearFocus() }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                TopBar(userName = user?.fullName, userViewModel = userViewModelDash)
                Spacer(modifier = Modifier.height(20.dp))
                SearchBar(searchQuery) { searchQuery = it }
                Spacer(modifier = Modifier.height(20.dp))
                // Promo Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(150.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = promo)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(0.6f)) {
                                Text(
                                    "Get Special Discounts",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    "up to 35%",
                                    color = Orange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                    Image(
                        painter = painterResource(R.drawable.rentrimage),
                        contentDescription = null,
                        modifier = Modifier
                            .size(180.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = 40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                // Categories Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Categories",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Show All",
                        color = if (selectedCategory == null) Color.Gray else Orange.copy(0.7f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            val categoryName = selectedCategory?.name ?: "All"
                            val intent = Intent(context, CategoryActivity::class.java)
                            intent.putExtra("Category", categoryName)
                            context.startActivity(intent)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Category Selection Row
            CategorySelection(selectedCategory?.name) { name ->
                selectedCategory = if (selectedCategory?.name == name) {
                    null // Deselect if same category clicked
                } else {
                    categories.find { it.name == name }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(20.dp))

                // Show products based on selection
                if (selectedCategory != null) {
                    // Show filtered products for selected category
                    if (filteredProducts.isNotEmpty()) {
                        Text(
                            "${selectedCategory!!.name} Products",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        ProductGrid(products = filteredProducts)
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No products found in this category",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    // Show dashboard preview when no category selected
                    if (displayedProducts.isNotEmpty()) {
                        Text(
                            "Featured Products",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        ProductGrid(products = displayedProducts)
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No products available",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun TopBar(userName: String?, userViewModel: UserViewModel) {
    val user by userViewModel.user.observeAsState(null)

    fun getInitials(name: String): String {
        val names = name.trim().split(" ")
        return if (names.size > 1) {
            "${names.first().firstOrNull() ?: ""}${names.last().firstOrNull() ?: ""}".uppercase()
        } else {
            (name.firstOrNull()?.toString() ?: "").uppercase()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(37.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Orange, Color(0xFFFFC66C))))
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2C2E))
            ) {
                if (!user?.profileImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = user?.profileImage,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = user?.fullName?.let { getInitials(it) } ?: "...",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Good Morning", color = Color.Gray, fontSize = 12.sp)
                Text(
                    text = userName ?: "...",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        IconButton(onClick = {
            // notifs
        }) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search products...", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Field,
            focusedIndicatorColor = Orange,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.White,
            focusedLeadingIconColor = Color.Black,
            unfocusedLeadingIconColor = Color.White
        )
    )
}

@Composable
fun CategorySelection(selectedCategory: String?, onCategorySelected: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category.name
            Box(
                modifier = Modifier
                    .clickable { onCategorySelected(category.name) }
                    .background(
                        if (isSelected) Orange else Field,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    category.name,
                    color = Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ProductGrid(products: List<ProductModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        products.chunked(2).forEach { rowProducts ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowProducts.forEach { product ->
                    Column(modifier = Modifier.weight(1f)) {
                        ProductCard(product)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            product.title,
                            color = Orange,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "NPR. ${product.price}/day",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                // Add empty space if odd number of products
                if (rowProducts.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: ProductModel) {
    val context = LocalContext.current
    val imageUrl = product.imageUrl.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ProductActivity::class.java)
                intent.putExtra("productId", product.productId)
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.height(120.dp)) {
            AsyncImage(
                model = imageUrl,
                placeholder = painterResource(id = R.drawable.rentrimage),
                error = painterResource(id = R.drawable.rentrimage),
                contentDescription = product.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Show availability badge
            if (!product.availability || product.outOfStock) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            if (product.outOfStock) Color.Red else Color.Yellow,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (product.outOfStock) "RENTED" else "UNAVAILABLE",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}