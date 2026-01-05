package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImpl
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
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val product by productViewModel.product.observeAsState()
    var sellerName by remember { mutableStateOf("") }
    var showFlagDialog by remember { mutableStateOf(false) }
    var rentalDays by remember { mutableStateOf(1) }

    // Flag reason state
    var selectedFlagReason by remember { mutableStateOf("") }
    var customFlagReason by remember { mutableStateOf("") }

    // Predefined flag reasons
    val flagReasons = listOf(
        "Inappropriate Content",
        "Fake/Scam Product",
        "Wrong Category",
        "Price Gouging",
        "Copyright Infringement",
        "Other"
    )

    val currentUserId = userViewModel.getCurrentUser()?.uid
    val isSeller = product?.listedBy == currentUserId

    // Check if current user already flagged this product
    val isAlreadyFlagged = remember(product, currentUserId) {
        product?.flaggedBy?.contains(currentUserId) == true
    }

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
            onDismissRequest = {
                showFlagDialog = false
                selectedFlagReason = ""
                customFlagReason = ""
            },
            title = { Text("Flag Item", color = Color.White) },
            text = {
                Column {
                    // Show warning if already flagged
                    if (isAlreadyFlagged) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = "Already Flagged",
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "You have already flagged this item",
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text("Please select a reason for flagging:", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Flag reason selection
                    flagReasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isAlreadyFlagged) {
                                        selectedFlagReason = reason
                                        if (reason != "Other") customFlagReason = ""
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFlagReason == reason,
                                onClick = {
                                    if (!isAlreadyFlagged) {
                                        selectedFlagReason = reason
                                        if (reason != "Other") customFlagReason = ""
                                    }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.Red,
                                    unselectedColor = Color.Gray
                                ),
                                enabled = !isAlreadyFlagged
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(reason, color = if (isAlreadyFlagged) Color.Gray else Color.White, fontSize = 14.sp)
                        }
                    }

                    // Custom reason input for "Other" option
                    if (selectedFlagReason == "Other") {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customFlagReason,
                            onValueChange = { if (!isAlreadyFlagged) customFlagReason = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Please specify the reason", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Red,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = Color.Red,
                                unfocusedLabelColor = Color.Gray
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isAlreadyFlagged
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "⚠️ Flagging will increase the seller's flag count. Admin will review this item.",
                        color = Color.Yellow,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isAlreadyFlagged) {
                            Toast.makeText(context, "You have already flagged this item", Toast.LENGTH_SHORT).show()
                            showFlagDialog = false
                            return@Button
                        }

                        val currentProduct = product ?: return@Button
                        val uid = currentUserId ?: return@Button

                        val finalReason = when {
                            selectedFlagReason == "Other" && customFlagReason.isNotEmpty() -> customFlagReason
                            selectedFlagReason.isNotEmpty() && selectedFlagReason != "Other" -> selectedFlagReason
                            else -> {
                                Toast.makeText(context, "Please select or specify a reason", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        }

                        productViewModel.flagProduct(
                            productId = productId,
                            userId = uid,
                            reason = finalReason
                        ) { success, message ->
                            if (success) {
                                // Update seller's flag count
                                userViewModel.incrementFlagCount(currentProduct.listedBy) { userSuccess, userMsg ->
                                    if (userSuccess) {
                                        Toast.makeText(context, "Item flagged successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Flagged but failed to update flag count: $userMsg", Toast.LENGTH_SHORT).show()
                                        // Note: In production, you might want to rollback the flag here
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Failed to flag: $message", Toast.LENGTH_SHORT).show()
                            }
                        }

                        showFlagDialog = false
                        selectedFlagReason = ""
                        customFlagReason = ""
                    },
                    enabled = !isAlreadyFlagged && selectedFlagReason.isNotEmpty() &&
                            (selectedFlagReason != "Other" || customFlagReason.isNotEmpty()),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAlreadyFlagged) Color.Gray else Color.Red
                    )
                ) {
                    Text(
                        if (isAlreadyFlagged) "Already Flagged" else "Submit Flag",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showFlagDialog = false
                        selectedFlagReason = ""
                        customFlagReason = ""
                    }
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            val safeProduct = product
            if (!isSeller && safeProduct?.availability == true && safeProduct.outOfStock == false) {
                ProductBottomBar(
                    product = safeProduct,
                    rentalDays = rentalDays,
                    onRentNowClick = {
                        if (currentUserId != null) {
                            val updatedProduct = safeProduct.copy(
                                rentalStatus = "pending",
                                rentalRequesterId = currentUserId,
                                rentalDays = rentalDays
                            )
                            productViewModel.updateProduct(safeProduct.productId, updatedProduct) { success, _ ->
                                if (success) {
                                    Toast.makeText(context, "Rental request sent!", Toast.LENGTH_SHORT).show()
                                    activity?.finish()
                                } else {
                                    Toast.makeText(context, "Failed to send rental request.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
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

                // Show flag information if product is flagged
                if (safeProduct.flagged && safeProduct.flaggedReason.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Field)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Flag Information", color = Color.Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Reason: ${safeProduct.flaggedReason.joinToString(", ")}",
                        color = Color.Red.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Flagged by ${safeProduct.flaggedBy.size} user(s)",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Field)
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
fun ProductBottomBar(product: ProductModel, rentalDays: Int, onRentNowClick: () -> Unit) {
    val rentalPrice = product.price * rentalDays
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
            Text(String.format("NPR. %.2f", rentalPrice), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onRentNowClick,
            colors = ButtonDefaults.buttonColors(containerColor = if (product.rentalStatus == "pending") Color.Gray else Orange),
            shape = RoundedCornerShape(16.dp),
            enabled = product.rentalStatus != "pending",
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (product.rentalStatus == "pending") "Request Sent" else "Request to Rent", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}