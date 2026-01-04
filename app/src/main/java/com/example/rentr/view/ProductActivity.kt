package com.example.rentr.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel

class ProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val productId = intent.getStringExtra("productId") ?: ""

        setContent {
            RentrTheme {
                ProductDisplay(productId)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductDisplay(productId: String) {
    val context = LocalContext.current
    val activity = context as? Activity

    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImp1()) }

    val product by productViewModel.product.observeAsState()
    var sellerName by remember { mutableStateOf("") }
    var showFlagDialog by remember { mutableStateOf(false) }
    var rentalDays by remember { mutableStateOf(1) }

    val currentUserId = userViewModel.getCurrentUser()?.uid
    val isSeller = product?.listedBy == currentUserId

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId) { _, _, _ -> }
        }
    }

    LaunchedEffect(product?.listedBy) {
        val sellerId = product?.listedBy ?: return@LaunchedEffect
        userViewModel.getUserById(sellerId) { success, _, user ->
            if (success && user != null) {
                sellerName = user.fullName
            }
        }
    }

    if (showFlagDialog) {
        AlertDialog(
            onDismissRequest = { showFlagDialog = false },
            title = { Text("Confirm Flag") },
            text = { Text("Are you sure you want to flag this item?") },
            confirmButton = {
                Button(
                    onClick = {
                        val currentProduct = product ?: return@Button
                        val uid = currentUserId ?: return@Button

                        val updatedFlaggedBy = currentProduct.flaggedBy.toMutableList().apply {
                            if (!contains(uid)) add(uid)
                        }
                        productViewModel.updateProduct(currentProduct.productId, currentProduct.copy(flaggedBy = updatedFlaggedBy)) { success, _ ->
                            if (success) {
                                productViewModel.getProductById(productId) { _, _, _ -> }
                            }
                        }
                        showFlagDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Yes, Flag") }
            },
            dismissButton = {
                TextButton(onClick = { showFlagDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            val safeProduct = product
            if (!isSeller && safeProduct?.availability == true && safeProduct.outOfStock == false) {
                val totalPrice = safeProduct.price * rentalDays
                BottomBar(
                    price = totalPrice,
                    onPayNowClick = {
                        val intent = Intent(context, CheckoutActivity::class.java).apply {
                            putExtra("productTitle", safeProduct.title)
                            putExtra("basePrice", safeProduct.price)
                            putExtra("rentalPrice", totalPrice)
                            putExtra("days", rentalDays)
                            putExtra("productId", safeProduct.productId)
                            putExtra("sellerId", safeProduct.listedBy)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    ) { padding ->
        val safeProduct = product
        if (safeProduct == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
            return@Scaffold
        }

        val isAlreadyFlagged = safeProduct.flaggedBy.contains(currentUserId)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            val pagerState = rememberPagerState { safeProduct.imageUrl.size }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                if (safeProduct.imageUrl.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = safeProduct.imageUrl[page],
                            contentDescription = "Product image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.rentrimage),
                            error = painterResource(id = R.drawable.rentrimage)
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.rentrimage),
                        contentDescription = "Product image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { activity?.finish() },
                        modifier = Modifier.background(Field.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    if (!isSeller) {
                        if (isAlreadyFlagged) {
                            Card(
                                shape = RoundedCornerShape(50),
                                colors = CardDefaults.cardColors(containerColor = Field.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Flag, contentDescription = "Flagged", tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Flagged", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        } else {
                            IconButton(
                                onClick = { showFlagDialog = true },
                                modifier = Modifier.background(Field.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Flag, "Flag item", tint = Color.Red)
                            }
                        }
                    }
                }
            }
            if (safeProduct.imageUrl.size > 1) {
                Row(
                    Modifier
                        .height(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Orange else Color.Gray
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(safeProduct.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    if (safeProduct.availability && !safeProduct.outOfStock) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Orange.copy(alpha = 0.15f))
                        ) {
                            Text(
                                text = "Available",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Orange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Orange, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    val ratingText = if (safeProduct.ratingCount > 0) {
                        String.format("%.1f (%d ratings)", safeProduct.rating, safeProduct.ratingCount)
                    } else {
                        "No reviews yet"
                    }
                    Text(ratingText, color = Color.Gray, fontSize = 14.sp)
                }

                // Day Selector
                if (!isSeller) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Field)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rental Days", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (rentalDays > 1) rentalDays-- },
                                modifier = Modifier.border(1.dp, Color.Gray, CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease days", tint = Color.White)
                            }
                            Text(
                                "$rentalDays",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            IconButton(
                                onClick = { rentalDays++ },
                                enabled = true,
                                modifier = Modifier.border(1.dp, Color.Gray, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase days", tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Field)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Listed By", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(sellerName, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Description", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(safeProduct.description, color = Color.Gray, fontSize = 14.sp)

                if (!isSeller && currentUserId != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Field)
                    Spacer(modifier = Modifier.height(16.dp))

                    val currentUserRating = safeProduct.ratedBy[currentUserId] ?: 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        RatingBar(
                            rating = currentUserRating,
                            onRatingChange = { newRating ->
                                productViewModel.updateRating(productId, currentUserId, newRating) { success, _ ->
                                    if (success) {
                                        Toast.makeText(context, "Rating submitted!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to submit rating.", Toast.LENGTH_SHORT).show()
                                    }
                                    productViewModel.getProductById(productId) { _, _, _ -> }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (currentUserRating > 0) {
                            TextButton(onClick = {
                                productViewModel.updateRating(productId, currentUserId, 0) { success, _ -> // 0 means remove rating
                                    if (success) {
                                        Toast.makeText(context, "Rating removed.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to remove rating.", Toast.LENGTH_SHORT).show()
                                    }
                                    productViewModel.getProductById(productId) { _, _, _ -> }
                                }
                            }) {
                                Text("Clear", color = Orange)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // Padding at the bottom
            }
        }
    }
}

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(modifier = modifier) {
        (1..5).forEach { index ->
            IconButton(onClick = { onRatingChange(index) }) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rate $index",
                    tint = if (index <= rating) Orange else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun BottomBar(price: Double, onPayNowClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Total price", color = Color.Gray, fontSize = 12.sp)
            Text(String.format("NPR. %.2f", price), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onPayNowClick,
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pay Now", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}