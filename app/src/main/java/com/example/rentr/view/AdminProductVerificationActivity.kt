package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import com.example.rentr.repository.NotificationRepoImpl
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.AdminProductViewModel
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
fun ProductVerificationScreen() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // --- 1. INITIALIZE REPOSITORIES (Fixes the ViewModel Crash) ---
    val productRepo = remember { ProductRepoImpl() }
    val userRepo = remember { UserRepoImpl() }
    val notifRepo = remember { NotificationRepoImpl(context.applicationContext) }

    // --- 2. INITIALIZE VIEWMODELS WITH FACTORIES ---
    val productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.Factory(productRepo)
    )
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModel.Factory(userRepo)
    )
    val adminViewModel: AdminProductViewModel = viewModel(
        factory = AdminProductViewModel.Factory(productRepo, notifRepo)
    )

    // SAFE INTENT HANDLING
    val intent = activity?.intent
    val productId = remember { intent?.getStringExtra("productId") ?: "" }
    val listedById = remember { intent?.getStringExtra("listedBy") ?: "" }

    val product by productViewModel.product.observeAsState()
    var sellerName by remember { mutableStateOf("Loading...") }
    var sellerEmail by remember { mutableStateOf("Loading...") }
    var sellerPhone by remember { mutableStateOf("") }

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }
    var showVerifyConfirmation by remember { mutableStateOf(false) }

    // Fetch Product
    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId) { success, msg, _ ->
                if(!success) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fetch Seller
    LaunchedEffect(product) {
        val currentProduct = product
        val sellerId = if (currentProduct != null) currentProduct.listedBy else listedById

        if (sellerId.isNotEmpty()) {
            userViewModel.getUserById(sellerId) { success, _, user ->
                if (success && user != null) {
                    sellerName = user.fullName
                    sellerEmail = user.email
                    sellerPhone = user.phoneNumber
                } else {
                    sellerName = "Unknown"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Verification", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
        ) {
            if (product == null) {
                Box(Modifier.fillMaxSize().height(300.dp), contentAlignment = Alignment.Center) {
                    if(productId.isEmpty()) Text("No Product ID", color=Color.Red)
                    else CircularProgressIndicator(color = Orange)
                }
            } else {
                val safeProduct = product!!

                Column(modifier = Modifier.padding(16.dp)) {
                    val images = safeProduct.imageUrl ?: emptyList()
                    ProductImagesSection(images)
                    Spacer(modifier = Modifier.height(24.dp))
                    ProductDetailsSection(safeProduct)
                    Spacer(modifier = Modifier.height(24.dp))
                    SellerInformationSection(safeProduct.listedBy, sellerName, sellerEmail, sellerPhone)
                    Spacer(modifier = Modifier.height(32.dp))
                    ActionButtonsSection(
                        isVerified = safeProduct.verified,
                        onRejectClick = { showRejectDialog = true },
                        onVerifyClick = { showVerifyConfirmation = true }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Product") },
            text = {
                Column {
                    Text("Reason for rejection:")
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionReason.isNotBlank() && product != null) {
                            adminViewModel.rejectProduct(productId, product!!.listedBy, rejectionReason)
                            showRejectDialog = false
                            Toast.makeText(context, "Product Rejected", Toast.LENGTH_SHORT).show()
                            activity?.finish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { showRejectDialog = false }) { Text("Cancel") } }
        )
    }

    if (showVerifyConfirmation) {
        AlertDialog(
            onDismissRequest = { showVerifyConfirmation = false },
            title = { Text("Verify Product") },
            text = { Text("Verify this product? User will be notified.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (product != null) {
                            adminViewModel.approveProduct(productId, product!!.listedBy)
                            showVerifyConfirmation = false
                            Toast.makeText(context, "Verified!", Toast.LENGTH_SHORT).show()
                            activity?.finish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                ) { Text("Yes, Verify") }
            },
            dismissButton = { TextButton(onClick = { showVerifyConfirmation = false }) { Text("Cancel") } }
        )
    }
}

// Reuse the Helper Composables (ProductImagesSection, etc.) from your existing code or previous pastes.
// (I omitted them here to save space, but ensure they are in the file)
@Composable
fun ProductImagesSection(imageUrls: List<String>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Field)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Product Images", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            if (imageUrls.isEmpty()) { Text("No images available", color = Color.Gray) }
            else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(imageUrls.size) { index ->
                        AsyncImage(model = imageUrls[index], contentDescription = null, modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop, placeholder = painterResource(R.drawable.rentrimage), error = painterResource(R.drawable.rentrimage))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailsSection(product: com.example.rentr.model.ProductModel) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Field)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Product Details", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            ProductDetailRow("Title", product.title ?: "No Title")
            Spacer(Modifier.height(8.dp))
            ProductDetailRow("Price", "NPR. ${product.price}")
            Spacer(Modifier.height(8.dp))
            ProductDetailRow("Category", product.category ?: "None")
        }
    }
}

@Composable
fun ProductDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray); Text(value, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SellerInformationSection(sellerId: String, sellerName: String, sellerEmail: String, sellerPhone: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Field)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Seller Info", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            SellerInfoRow(Icons.Default.Person, "Name", sellerName)
            SellerInfoRow(Icons.Default.Email, "Email", sellerEmail)
            if (sellerPhone.isNotEmpty()) SellerInfoRow(Icons.Default.Phone, "Phone", sellerPhone)
        }
    }
}

@Composable
fun SellerInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, tint = Orange, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp))
        Column { Text(label, color = Color.Gray, fontSize = 12.sp); Text(value, color = Color.White, fontSize = 14.sp) }
    }
}

@Composable
fun ActionButtonsSection(isVerified: Boolean, onRejectClick: () -> Unit, onVerifyClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = onRejectClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), enabled = !isVerified) { Text("Reject") }
        Button(onClick = onVerifyClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Green), enabled = !isVerified) { Text("Verify") }
    }
}