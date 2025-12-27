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
import androidx.compose.ui.text.font.FontStyle
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

    var showFlagReasonDialog by remember { mutableStateOf(false) }
    var showFlagConfirmationDialog by remember { mutableStateOf(false) }
    var flagReason by remember { mutableStateOf("") }

    val currentUserId = userViewModel.getCurrentUser()?.uid
    val isSeller = product?.listedBy == currentUserId
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

    val randomPrice = remember { (100..2000).random().toDouble() }
    val productRating = remember { "%.1f".format((3..5).random().toFloat() + (0..9).random().toFloat() / 10) }
    val totalPrice = randomPrice

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
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
                                        showFlagReasonDialog = true
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
                        if (product!!.availability && !product!!.outOfStock) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (product!!.availability) Orange.copy(alpha = 0.15f) else Field
                                )
                            ) {
                                Text(
                                    text = "Available",
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    color = if (product!!.availability) Orange else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Orange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$productRating (4,749 reviews)", color = Color.Gray, fontSize = 14.sp)
                    }

                    if (product!!.availability && !product!!.outOfStock) {
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
                        Spacer(modifier = Modifier.height(20.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Red.copy(alpha = 0.8f)
                                )
                            ) {
                                Text(
                                    text = if (!product!!.availability) "Unavailable" else "Out of Stock",
                                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFlagReasonDialog) {
        AlertDialog(
            onDismissRequest = {
                showFlagReasonDialog = false
                flagReason = ""
            },
            title = {
                Text(
                    "Report Product",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Please tell us why you're flagging this product:",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = flagReason,
                        onValueChange = { flagReason = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = {
                            Text(
                                "Enter your reason here...",
                                color = Color.Gray.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Field,
                            unfocusedContainerColor = Field,
                            focusedIndicatorColor = Orange,
                            unfocusedIndicatorColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = false,
                        maxLines = 5,
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your report will be reviewed by our admin team.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showFlagReasonDialog = false
                            flagReason = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Field
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                    Button(
                        onClick = {
                            if (flagReason.trim().isNotEmpty()) {
                                showFlagReasonDialog = false
                                showFlagConfirmationDialog = true
                            } else {
                                Toast.makeText(context, "Please enter a reason", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Continue", color = Color.White)
                    }
                }
            },
            containerColor = Color.Black
        )
    }

    if (showFlagConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                showFlagConfirmationDialog = false
                flagReason = ""
            },
            title = {
                Text(
                    "Confirm Report",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to report this product?",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Field.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Your Report Reason:",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                flagReason,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "The seller will be notified and admin will review your report.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showFlagConfirmationDialog = false
                            flagReason = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Field
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                    Button(
                        onClick = {
                            val currentProduct = product
                            if (currentUserId != null && currentProduct != null) {
                                val updatedFlaggedBy = currentProduct.flaggedBy.toMutableList().apply {
                                    if (!contains(currentUserId)) add(currentUserId)
                                }

                                val updatedProduct = currentProduct.copy(
                                    flaggedBy = updatedFlaggedBy,
                                    flagged = true,
                                    flagReason = flagReason
                                )

                                productViewModel.updateProduct(
                                    currentProduct.productId,
                                    updatedProduct
                                ) { success, message ->
                                    if (success) {
                                        // Increase flag count for seller
                                        val sellerId = currentProduct.listedBy
                                        userViewModel.getUserById(sellerId) { sellerSuccess, _, seller ->
                                            if (sellerSuccess && seller != null) {
                                                val updatedSeller = seller.copy(
                                                    flagCount = seller.flagCount + 1
                                                )
                                                userViewModel.updateProfile(
                                                    sellerId,
                                                    updatedSeller
                                                ) { _, _ ->
                                                    productViewModel.getProductById(productId) { _, _, _ -> }

                                                    Toast.makeText(
                                                        context,
                                                        "Product reported successfully!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Failed to report product: $message",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            showFlagConfirmationDialog = false
                            flagReason = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Yes, Report", color = Color.White)
                    }
                }
            },
            containerColor = Color.Black
        )
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
            Text("NPR. $price", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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