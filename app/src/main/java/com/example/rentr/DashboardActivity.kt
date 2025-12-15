package com.example.rentr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.promo
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

data class Product(val id: Int, val name: String, val category: String, val imageRes: Int)
data class Category(val name: String, val icon: ImageVector)

val categories = listOf(
    Category("Bicycle", Icons.Default.DirectionsBike),
    Category("Bike", Icons.Default.Motorcycle),
    Category("Camera", Icons.Default.CameraAlt),
    Category("Car", Icons.Default.DirectionsCar),
    Category("Toy", Icons.Default.Toys),
    Category("Furniture", Icons.Default.Chair),
    Category("Laptop", Icons.Default.Laptop),
    Category("Kitchen", Icons.Default.Kitchen),
)

@Composable
fun DashboardScreen() {

    val userViewModelDash = remember { UserViewModel(UserRepoImp1()) }
    val user by userViewModelDash.user.observeAsState(null)

    LaunchedEffect(Unit) {
        userViewModelDash.getCurrentUser()?.uid?.let { userId ->
            userViewModelDash.getUserById(userId) { _, _, _ ->
                // We are observing the user LiveData, so no action is needed here
            }
        }
    }

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val allProducts = remember {
        categories.flatMap { category ->
            (1..6).map { i ->
                val productName = when (category.name) {
                    "Bicycle" -> if (i % 2 == 1) "Mountain Bike" else "City Bicycle"
                    "Bike" -> if (i % 2 == 1) "Sports Bike" else "Cruiser Bike"
                    "Camera" -> if (i % 2 == 1) "DSLR Camera" else "Mirrorless Camera"
                    "Car" -> if (i % 2 == 1) "Modern Sedan" else "Luxury SUV"
                    "Toy" -> if (i % 2 == 1) "Action Figure" else "Building Blocks"
                    "Furniture" -> if (i % 2 == 1) "Modern Chair" else "Wooden Table"
                    "Laptop" -> if (i % 2 == 1) "Gaming Laptop" else "Ultrabook"
                    "Kitchen" -> if (i % 2 == 1) "Blender" else "Toaster"
                    else -> "Product"
                }
                val imageRes = when (category.name) {
                    "Bicycle" -> R.drawable.bicycle
                    "Bike" -> R.drawable.bike
                    "Camera" -> R.drawable.camera
                    "Car" -> R.drawable.car
                    "Toy" -> R.drawable.toy
                    "Furniture" -> R.drawable.bicycle
                    "Laptop" -> R.drawable.camera
                    "Kitchen" -> R.drawable.bike
                    else -> R.drawable.bicycle
                }
                Product(0, productName, category.name, imageRes)
            }
        }.mapIndexed { index, product -> product.copy(id = index + 1) }
    }

    val filteredProducts = allProducts.filter {
        selectedCategory != null &&
                it.category == selectedCategory?.name &&
                it.name.contains(searchQuery, ignoreCase = true)
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
                TopBar(userName = user?.fullName)
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
                            coroutineScope.launch {
                                if (selectedCategory == null) {
                                    snackbarHostState.showSnackbar("Please choose a category")
                                } else {
                                    val intent = Intent(context, CategoryActivity::class.java)
                                    intent.putExtra("categoryName", selectedCategory?.name)
                                    context.startActivity(intent)
                                }
                            }
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
                    ProductGrid(filteredProducts)
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun TopBar(userName: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile Image",
                tint = Color.White,
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
            )
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
fun ProductGrid(products: List<Product>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        products.chunked(2).forEach { rowProducts ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                rowProducts.forEach { product ->
                    Column(modifier = Modifier.weight(1f)) {
                        ProductCard(product)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(product.name, color = Orange, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "NPR. ${(product.id * 157 % 1000) + 500}",
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
fun ProductCard(product: Product) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ProductActivity::class.java)
                intent.putExtra("productName", product.name)
                intent.putExtra("productImg", product.imageRes)
                intent.putExtra("productPrice", product.id * 157 % 1000 + 500)
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field)
    ) {
        Image(
            painter = painterResource(product.imageRes),
            contentDescription = null,
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
