package com.example.rentr.view

import android.R.attr.onClick
import android.content.Intent
import android.os.Bundle
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
private val primaryColor = Color.Black
private val secondaryColor = Color(0xFF2A2A2A)
private val accentColor = Color(0xFFFF6200)
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
private val cardBackgroundColor = Color(0xFF2C2C2E)

@Composable
fun DashboardScreen() {

    val userViewModelDash = remember { UserViewModel(UserRepoImp1()) }
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val user by userViewModelDash.user.observeAsState(null)
    val products by productViewModel.allProducts.observeAsState()

    LaunchedEffect(Unit) {
        userViewModelDash.getCurrentUser()?.uid?.let { userId ->
            userViewModelDash.getUserById(userId) { _, _, _ ->
                // LiveData observer will handle user data update
            }
        }
    }

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedCategory) {
        selectedCategory?.let { category ->
            productViewModel.getAllProductsByCategory(category.name) { _, _, _ ->
                // LiveData will update the product list
            }
        }
    }

        val filteredProducts = products?.filter {
           it.verified && !it.flagged && it.title.contains(searchQuery, ignoreCase = true)
        } ?: emptyList()

        // Limit to 6 items for the dashboard preview
        val displayedProducts = filteredProducts.take(6)

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
                TopBar(userName = user?.fullName, userViewModelDash)
                Spacer(modifier = Modifier.height(20.dp))
                SearchBar(searchQuery) { searchQuery = it }
                Spacer(modifier = Modifier.height(20.dp))
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
                                Text("Get Special Discounts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("up to 35%", color = Orange, fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Categories", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Show All",
                        color = if (selectedCategory == null) Color.Gray else Orange.copy(0.7f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            val intent = Intent (context,CategoryActivity::class.java)
                            intent.putExtra("Category",selectedCategory?.name)
                            context.startActivity(intent)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            CategorySelection(selectedCategory?.name) { name ->
                selectedCategory =
                    if (selectedCategory?.name == name) null else categories.find { it.name == name }
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(20.dp))
                if (selectedCategory != null) {
                    ProductGrid(products = displayedProducts)
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}


@Composable
fun TopBar(userName: String?,userViewModel: UserViewModel) {
    val user by userViewModel.user.observeAsState(null)

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
                    .padding(2.dp) // Simulates border
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
                Text("Good Morning", color = Color.Gray, fontSize = 12.sp)
                Text(
                    text = userName ?: "...",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search", color = Color.Gray) },
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
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category.name
            Box(
                modifier = Modifier
                    .clickable { onCategorySelected(category.name) }
                    .background(if (isSelected) Orange else Field, RoundedCornerShape(16.dp))
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
    val imageUrl = product.imageUrl.firstOrNull()

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
            model = imageUrl,
            placeholder = painterResource(id = R.drawable.rentrimage),
            error = painterResource(id = R.drawable.rentrimage),
            contentDescription = product.title,
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}
