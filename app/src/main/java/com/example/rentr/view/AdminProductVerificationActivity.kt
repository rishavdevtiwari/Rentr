package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel

class AdminProductVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                ProductVerificationScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductVerificationScreen(
    productViewModel: ProductViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProductViewModel(ProductRepoImpl()) as T
            }
        }
    ),
    userViewModel: UserViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return UserViewModel(UserRepoImp1()) as T
            }
        }
    )
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val intent = activity?.intent ?: remember { Intent() }

    val productId = remember { intent.getStringExtra("productId") ?: "" }
    val productName = remember { intent.getStringExtra("productName") ?: "" }
    val listedById = remember { intent.getStringExtra("listedBy") ?: "" }

    val product by productViewModel.product.observeAsState()
    var sellerName by remember { mutableStateOf("") }
    var sellerEmail by remember { mutableStateOf("") }
    var sellerPhone by remember { mutableStateOf("") }
    var showRejectConfirmation by remember { mutableStateOf(false) }
    var showVerifyConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId) { _, _, _ -> }
        }
    }

    LaunchedEffect(product?.listedBy) {
        val sellerId = product?.listedBy ?: listedById
        if (sellerId.isNotEmpty()) {
            userViewModel.getUserById(sellerId) { success, _, user ->
                if (success && user != null) {
                    sellerName = user.fullName
                    sellerEmail = user.email
                    sellerPhone = user.phoneNumber
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Product Verification", color = Color.White, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { (context as? android.app.Activity)?.finish() }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (product == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Orange)
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Product Images (Scrollable)
                    ProductImagesSection(product!!.imageUrl)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Product Details
                    ProductDetailsSection(product!!)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Seller Information
                    SellerInformationSection(
                        sellerId = product!!.listedBy,
                        sellerName = sellerName,
                        sellerEmail = sellerEmail,
                        sellerPhone = sellerPhone
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Action Buttons
                    ActionButtonsSection(
                        productId = productId,
                        isVerified = product!!.verified,
                        onRejectClick = { showRejectConfirmation = true },
                        onVerifyClick = { showVerifyConfirmation = true },
                        productViewModel = productViewModel
                    )
                }
            }
        }
    }

    // Confirmation Dialogs
    if (showRejectConfirmation) {
        AlertDialog(
            onDismissRequest = { showRejectConfirmation = false },
            title = { Text("Reject Product") },
            text = { Text("Are you sure you want to reject this product? The product will be removed from the platform.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productViewModel.updateProduct(
                            productId,
                            product!!.copy(verified = false),
                            callback = { success, message ->
                                if (success) {
                                    productViewModel.deleteProduct(productId) { _, _ ->
                                        showRejectConfirmation = false
                                        (context as? android.app.Activity)?.finish()
                                    }
                                }
                            }
                        )
                    }
                ) {
                    Text("Yes, Reject")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRejectConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showVerifyConfirmation) {
        AlertDialog(
            onDismissRequest = { showVerifyConfirmation = false },
            title = { Text("Verify Product") },
            text = { Text("Are you sure you want to verify this product?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productViewModel.updateProduct(
                            productId,
                            product!!.copy(verified = true),
                            callback = { success, message ->
                                if (success) {
                                    showVerifyConfirmation = false
                                    (context as? android.app.Activity)?.finish()
                                }
                            }
                        )
                    }
                ) {
                    Text("Yes, Verify")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showVerifyConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProductImagesSection(imageUrls: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Product Images",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(imageUrls.size) { index ->
                    AsyncImage(
                        model = imageUrls[index],
                        contentDescription = "Product image ${index + 1}",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.rentrimage),
                        error = painterResource(id = R.drawable.rentrimage)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductDetailsSection(product: com.example.rentr.model.ProductModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Product Details",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Product Fields Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProductDetailRow("Title", product.title)
                ProductDetailRow("Price", "NPR. ${product.price}")
                ProductDetailRow("Category", product.category)
                ProductDetailRow("Quantity", product.quantity.toString())
                ProductDetailRow("Availability", if (product.availability) "Available" else "Not Available")
                ProductDetailRow("Stock Status", if (product.outOfStock) "Out of Stock" else "In Stock")
                ProductDetailRow("Rating", "${product.rating} (${product.ratingCount} reviews)")
                ProductDetailRow("Verification Status", if (product.verified) "Verified" else "Pending")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Column {
                Text(
                    "Description",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    product.description,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ProductDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(
            value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SellerInformationSection(sellerId: String, sellerName: String, sellerEmail: String, sellerPhone: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Seller Information",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SellerInfoRow(Icons.Default.Person, "User ID", sellerId)
                SellerInfoRow(Icons.Default.Badge, "Full Name", sellerName)
                SellerInfoRow(Icons.Default.Email, "Email", sellerEmail)
                SellerInfoRow(Icons.Default.Phone, "Phone", sellerPhone)
            }
        }
    }
}

@Composable
fun SellerInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Orange,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(
                value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActionButtonsSection(
    productId: String,
    isVerified: Boolean,
    onRejectClick: () -> Unit,
    onVerifyClick: () -> Unit,
    productViewModel: ProductViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onRejectClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),
            enabled = !isVerified
        ) {
            Icon(Icons.Default.Close, contentDescription = "Reject")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reject Product")
        }

        Button(
            onClick = onVerifyClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Green
            ),
            enabled = !isVerified
        ) {
            Icon(Icons.Default.Check, contentDescription = "Verify")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verify Product")
        }
    }
}