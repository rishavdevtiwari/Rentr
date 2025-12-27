package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    // Get current user ID
    val currentUserId = userViewModel.getCurrentUser()?.uid

    // Determine if the current user is the seller
    val isSeller = product?.listedBy == currentUserId

    // Determine if the item is already flagged by the current user
    val isAlreadyFlagged = product?.flaggedBy?.contains(currentUserId) == true


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

    // --- Flag Confirmation Dialog ---
    if (showFlagDialog) {
        AlertDialog(
            onDismissRequest = { showFlagDialog = false },
            title = { Text("Confirm Flag") },
            text = { Text("Are you sure you want to flag this item?") },
            confirmButton = {
                Button(
                    onClick = {
                        val currentProduct = product
                        if (currentUserId != null && currentProduct != null) {
                            // Add user ID to the flaggedBy list
                            val updatedFlaggedBy = currentProduct.flaggedBy.toMutableList().apply {
                                if (!contains(currentUserId)) add(currentUserId)
                            }
                            // Create an updated product model
                            val updatedProduct = currentProduct.copy(flaggedBy = updatedFlaggedBy)

                            // Use the existing updateProduct function
                            productViewModel.updateProduct(currentProduct.productId, updatedProduct) { success, _ ->
                                if (success) {
                                    // Refresh the product details to reflect the change immediately
                                    productViewModel.getProductById(productId) { _, _, _ -> }
                                }
                            }
                        }
                        showFlagDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes, Flag")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFlagDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    val randomPrice = remember { (100..2000).random().toDouble() }
    val totalPrice = randomPrice

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            // Show BottomBar only if the user is NOT the seller and the product is available
            if (!isSeller && product?.availability == true && product?.outOfStock == false) {
                BottomBar(price = totalPrice)
            }
        }
    ) { padding ->
        if (product == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Slider
                val pagerState = rememberPagerState(pageCount = { product!!.imageUrl.size })
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = product!!.imageUrl[page],
                            contentDescription = "Product image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.rentrimage),
                            error = painterResource(id = R.drawable.rentrimage)
                        )
                    }

                    // Top Bar with Back and Flag buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        IconButton(
                            onClick = { activity?.finish() },
                            modifier = Modifier
                                .background(Field.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }

                        // Flag Button / Flagged Indicator - Visible only if NOT the seller
                        if (!isSeller) {
                            if (isAlreadyFlagged) {
                                // "Flagged" indicator (disabled)
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
                                // Active Flag Button
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


                // Slider Indicator
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

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product!!.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        if (product!!.availability && !product!!.outOfStock) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Orange.copy(alpha = 0.15f)
                                )
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

                    // --- Dynamic Rating Display ---
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Orange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        val ratingText = if (product!!.ratingCount > 0) {
                            String.format("%.1f (%d reviews)", product!!.rating, product!!.ratingCount)
                        } else {
                            "No reviews yet"
                        }
                        Text(ratingText, color = Color.Gray, fontSize = 14.sp)
                    }

                    // --- User Rating Section ---
                    if (!isSeller && currentUserId != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Field)
                        Spacer(modifier = Modifier.height(16.dp))

                        val currentUserRating = product!!.ratedBy[currentUserId] ?: 0

                        Column {
                            Text("Your Rating", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RatingBar(
                                    rating = currentUserRating,
                                    onRatingChange = { newRating ->
                                        productViewModel.updateRating(productId, currentUserId, newRating) { success, _ ->
                                            if (success) {
                                                Toast.makeText(context, "Rating submitted!", Toast.LENGTH_SHORT).show()
                                                productViewModel.getProductById(productId) { _, _, _ -> }
                                            } else {
                                                Toast.makeText(context, "Failed to submit rating.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (currentUserRating > 0) {
                                    TextButton(onClick = {
                                        productViewModel.updateRating(productId, currentUserId, 0) { success, _ -> // 0 means remove rating
                                            if (success) {
                                                Toast.makeText(context, "Rating removed.", Toast.LENGTH_SHORT).show()
                                                productViewModel.getProductById(productId) { _, _, _ -> }
                                            }
                                        }
                                    }) {
                                        Text("Clear", color = Orange)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Field)

                    // --- Listed By Section ---
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Listed By", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(sellerName, color = Color.Gray, fontSize = 14.sp)

                    // --- Description Section ---
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Description", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(product!!.description, color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(100.dp)) // Padding at the bottom
                }
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
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}


@Composable
fun BottomBar(price: Double) {
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
            Text("NPR. ${price}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pay to Rent", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}