package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
    val sellerViewModel = remember { UserViewModel(UserRepoImp1()) }
    val currentUserViewModel = remember { UserViewModel(UserRepoImp1()) }

    val product by productViewModel.product.observeAsState()
    val currentUser by currentUserViewModel.user.observeAsState()
    var sellerName by remember { mutableStateOf("") }

    var showFlagReasonDialog by remember { mutableStateOf(false) }

    val currentUserId = currentUserViewModel.getCurrentUser()?.uid
    val isSeller = product?.listedBy == currentUserId
    val isAlreadyFlagged = product?.flaggedBy?.contains(currentUserId) == true
    val isUserVerified = currentUser?.verified == true

    LaunchedEffect(currentUserId) {
        currentUserId?.let {
            currentUserViewModel.getUserById(it) { _, _, _ -> }
        }
    }

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId) { _, _, _ -> }
        }
    }

    LaunchedEffect(product?.listedBy) {
        val sellerId = product?.listedBy ?: return@LaunchedEffect
        sellerViewModel.getUserById(sellerId) { success, _, user ->
            if (success && user != null) {
                sellerName = user.fullName
            }
        }
    }
    if (showFlagReasonDialog) {
        var flagReasonText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showFlagReasonDialog = false },
            title = { Text("Report Item") },
            text = {
                Column {
                    Text("Please provide a reason for flagging this item.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = flagReasonText,
                        onValueChange = { flagReasonText = it },
                        placeholder = { Text("Reason...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentProduct = product
                        if (currentUserId != null && currentProduct != null) {
                            val updatedFlaggedBy = currentProduct.flaggedBy.toMutableList().apply {
                                if (!contains(currentUserId)) add(currentUserId)
                            }
                            val updatedFlagReasons = (currentProduct.flaggedReason ?: emptyList()).toMutableList().apply {
                                add(flagReasonText)
                            }
                            val updatedProduct = currentProduct.copy(
                                flaggedBy = updatedFlaggedBy,
                                flaggedReason = updatedFlagReasons
                            )
                            productViewModel.updateProduct(currentProduct.productId, updatedProduct) { success, _ ->
                                if (success) {
                                    Toast.makeText(context, "Item flagged. Thank you for your feedback.", Toast.LENGTH_SHORT).show()
                                    productViewModel.getProductById(productId) { _, _, _ -> }
                                } else {
                                    Toast.makeText(context, "Failed to flag item.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        showFlagReasonDialog = false
                    },
                    enabled = flagReasonText.isNotBlank()
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFlagReasonDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            if (!isSeller) {
                BottomBar(
                    price = product?.price ?: 0.0,
                    enabled = product?.availability == true && product?.outOfStock == false,
                    onClick = {
                        if (isUserVerified) {
                            // TODO: Proceed to payment
                            Toast.makeText(context, "Proceeding to payment...", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please complete KYC to rent items.", Toast.LENGTH_LONG).show()
                        }
                    }
                )
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
                            model = product!!.imageUrl.getOrNull(page) ?: "",
                            contentDescription = "Product image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.rentrimage),
                            error = painterResource(id = R.drawable.rentrimage)
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
                            modifier = Modifier
                                .background(Field.copy(alpha = 0.5f), CircleShape)
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
                                    onClick = {
                                        if (isUserVerified) {
                                            showFlagReasonDialog = true
                                        } else {
                                            Toast.makeText(context, "Please verify your account to flag items.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.background(Field.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Flag, "Flag item", tint = Color.Red)
                                }
                            }
                        }
                    }
                }

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

                        val isAvailable = product!!.availability && !product!!.outOfStock
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAvailable) Orange.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                            )
                        ) {
                            val statusText = when {
                                isAvailable -> "Available"
                                !product!!.availability -> "Unavailable"
                                else -> "Rented Out"
                            }
                            val statusColor = if (isAvailable) Orange else Color.Gray
                            Text(
                                text = statusText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "NPR. ${product!!.price}/day",
                            color = Orange,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Orange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "${product!!.rating} (${product!!.ratingCount} reviews)",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Field)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Listed By", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(sellerName, color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Field)
                    Text("Description", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(product!!.description, color = Color.Gray, fontSize = 14.sp)

                    if (!isSeller && currentUserId != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Field)
                        Spacer(modifier = Modifier.height(16.dp))

                        val currentUserRating = product!!.ratedBy[currentUserId] ?: 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            RatingBar(
                                rating = currentUserRating,
                                onRatingChange = { newRating ->
                                    if (!isUserVerified) {
                                        Toast.makeText(context, "Please verify your account to rate items.", Toast.LENGTH_SHORT).show()
                                        return@RatingBar
                                    }
                                    val currentProduct = product ?: return@RatingBar
                                    val newRatedBy = currentProduct.ratedBy.toMutableMap().apply { this[currentUserId] = newRating }
                                    val newRatingCount = newRatedBy.size
                                    val newAverageRating = if (newRatingCount > 0) newRatedBy.values.sum().toDouble() / newRatingCount else 0.0
                                    val optimisticallyUpdatedProduct = currentProduct.copy(
                                        ratedBy = newRatedBy,
                                        ratingCount = newRatingCount,
                                        rating = newAverageRating
                                    )
                                    productViewModel.product.postValue(optimisticallyUpdatedProduct)

                                    productViewModel.updateRating(productId, currentUserId, newRating) { success, _ ->
                                        if (success) {
                                            Toast.makeText(context, "Rating submitted!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to submit rating.", Toast.LENGTH_SHORT).show()
                                            productViewModel.product.postValue(currentProduct) // Revert on failure
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (currentUserRating > 0) {
                                TextButton(onClick = {
                                    if (!isUserVerified) {
                                        Toast.makeText(context, "Please verify your account to change your rating.", Toast.LENGTH_SHORT).show()
                                        return@TextButton
                                    }
                                    val currentProduct = product ?: return@TextButton
                                    val newRatedBy = currentProduct.ratedBy.toMutableMap().apply { remove(currentUserId) }
                                    val newRatingCount = newRatedBy.size
                                    val newAverageRating = if (newRatingCount > 0) newRatedBy.values.sum().toDouble() / newRatingCount else 0.0
                                    val optimisticallyUpdatedProduct = currentProduct.copy(
                                        ratedBy = newRatedBy,
                                        ratingCount = newRatingCount,
                                        rating = newAverageRating
                                    )
                                    productViewModel.product.postValue(optimisticallyUpdatedProduct)

                                    productViewModel.updateRating(productId, currentUserId, 0) { success, _ ->
                                        if (success) {
                                            Toast.makeText(context, "Rating removed.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to remove rating.", Toast.LENGTH_SHORT).show()
                                            productViewModel.product.postValue(currentProduct) // Revert on failure
                                        }
                                    }
                                }) {
                                    Text("Clear", color = Orange)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
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
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}


@Composable
fun BottomBar(price: Double, enabled: Boolean, onClick: () -> Unit) {
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
            Text("NPR. $price", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange,
                contentColor = Color.Black,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.Black.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pay to Rent", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}