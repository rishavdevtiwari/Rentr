package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.viewmodel.AdminFlagViewModel // IMPORT NEW VIEWMODEL
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel

private val primaryColor = Color(0xFF1E1E1E)
private val accentColor = Color(0xFFFF6200)
private val successColor = Color(0xFF4CAF50)
private val warningColor = Color(0xFFFF9800)
private val errorColor = Color(0xFFF44336)
private val infoColor = Color(0xFF2196F3)
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
private val cardBackgroundColor = Color(0xFF2C2C2E)

class AdminFlaggedReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val productId = intent.getStringExtra("productId") ?: ""
        setContent {
            FlagReviewScreen(productId = productId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagReviewScreen(productId: String) {
    val context = LocalContext.current
    val activity = context as? Activity

    // --- VIEWMODELS ---
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    // Add the Notification/Logic ViewModel
    val adminFlagViewModel: AdminFlagViewModel = viewModel()

    val product by productViewModel.product.observeAsState()
    var sellerInfo by remember { mutableStateOf<com.example.rentr.model.UserModel?>(null) }
    var flaggerInfo by remember { mutableStateOf<com.example.rentr.model.UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // --- DIALOG STATES ---
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResolveDialog by remember { mutableStateOf(false) }
    var showMarkDialog by remember { mutableStateOf(false) }

    // New Dialog for deleting user
    var showDeleteUserDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId) { success, _, productData ->
                if (success && productData != null) {
                    userViewModel.getUserById(productData.listedBy) { sellerSuccess, _, seller ->
                        if (sellerSuccess) sellerInfo = seller
                    }

                    productData.flaggedBy.firstOrNull()?.let { flaggerId ->
                        userViewModel.getUserById(flaggerId) { flaggerSuccess, _, flagger ->
                            if (flaggerSuccess) flaggerInfo = flagger
                        }
                    }
                }
                isLoading = false
            }
        }
    }

    fun getProductStatus(): String {
        return when {
            product?.appealReason?.isNotEmpty() == true -> "APPEALED"
            product?.flagged == true -> "UNDER REVIEW"
            else -> "RESOLVED"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Flag Review - ${product?.title ?: ""}",
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor
                )
            )
        },
        containerColor = primaryColor
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accentColor)
            }
        } else if (product == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Product not found", color = textColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Status Badge
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (getProductStatus()) {
                                    "APPEALED" -> infoColor.copy(alpha = 0.9f)
                                    "RESOLVED" -> successColor.copy(alpha = 0.9f)
                                    else -> warningColor.copy(alpha = 0.9f)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(getProductStatus(), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                    // --- SELLER INFO CARD (Updated with Delete User Button) ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Person, contentDescription = "Seller", tint = errorColor, modifier = Modifier.size(20.dp))
                                    Text("Seller Information", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }

                                // DELETE USER BUTTON (Icon)
                                IconButton(onClick = { showDeleteUserDialog = true }) {
                                    Icon(Icons.Default.PersonRemove, contentDescription = "Delete User", tint = errorColor)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            sellerInfo?.let { seller ->
                                FlagInfoRow(Icons.Default.Person, "Name", seller.fullName, textLightColor)
                                FlagInfoRow(Icons.Default.Email, "Email", seller.email, textLightColor)
                                FlagInfoRow(Icons.Default.Flag, "Flag Count", seller.flagCount.toString(), textLightColor)
                                if (seller.phoneNumber.isNotEmpty()) {
                                    FlagInfoRow(Icons.Default.Phone, "Phone", seller.phoneNumber, textLightColor)
                                }
                            } ?: Text("Loading seller info...", color = textLightColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- PRODUCT DETAILS CARD ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // ... Product Image and details (Same as original) ...
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = "Product", tint = warningColor, modifier = Modifier.size(20.dp))
                                Text("Product Details", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            product?.imageUrl?.firstOrNull()?.let { imageUrl ->
                                Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp))) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = product?.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(id = R.drawable.rentrimage),
                                        error = painterResource(id = R.drawable.rentrimage)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            Text(product?.title ?: "", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(product?.description ?: "", color = textLightColor, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(product?.category ?: "", color = textLightColor, fontSize = 14.sp)
                                Text("NPR. ${"%.2f".format(product?.price ?: 0.0)}", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- FLAG DETAILS CARD ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // ... Flag details (Same as original) ...
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                                Text("Flag Details", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Reason for Flagging:", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (product?.flaggedReason?.isNotEmpty() == true) {
                                Text(product!!.flaggedReason.joinToString(", "), color = textLightColor, fontSize = 13.sp)
                            } else {
                                Text("No reason provided", color = textLightColor, fontSize = 13.sp, fontStyle = FontStyle.Italic)
                            }
                            if (product?.appealReason?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Seller Appeal:", color = Color.Cyan, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(product!!.appealReason, color = Color.Cyan.copy(alpha = 0.8f), fontSize = 13.sp, fontStyle = FontStyle.Italic)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- ACTION BUTTONS ---
                    if (product?.flagged == true) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = errorColor)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Delete", color = textColor)
                                }

                                Button(
                                    onClick = { showResolveDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = successColor)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Resolve", modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Resolve", color = textColor)
                                }
                            }
                        }
                    } else {
                        // ... Already resolved state (Same as original) ...
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = successColor.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = successColor, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Flag Already Resolved", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // --- 1. DELETE PRODUCT DIALOG ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Product", color = textColor, fontWeight = FontWeight.Bold) },
            text = { Text("Permanently delete this product? The user will be notified.", color = textLightColor) },
            confirmButton = {
                Button(
                    onClick = {
                        product?.let { currentProduct ->
                            // CALL VIEWMODEL TO DELETE + NOTIFY
                            adminFlagViewModel.deleteProduct(currentProduct)

                            Toast.makeText(context, "Product Deleted & User Notified", Toast.LENGTH_SHORT).show()
                            activity?.finish()
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor)
                ) {
                    Text("DELETE", color = textColor)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor)) {
                    Text("Cancel", color = textColor)
                }
            },
            containerColor = primaryColor
        )
    }

    // --- 2. RESOLVE FLAG DIALOG ---
    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = { Text("Resolve Flag", color = textColor, fontWeight = FontWeight.Bold) },
            text = { Text("Clear the flag and restore the product? User will be notified.", color = textLightColor) },
            confirmButton = {
                Button(
                    onClick = {
                        product?.let { currentProduct ->
                            // CALL VIEWMODEL TO RESOLVE + NOTIFY
                            adminFlagViewModel.resolveFlag(currentProduct)

                            Toast.makeText(context, "Flag Resolved & User Notified", Toast.LENGTH_SHORT).show()
                            activity?.finish()
                        }
                        showResolveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = successColor)
                ) {
                    Text("Resolve", color = textColor)
                }
            },
            dismissButton = {
                Button(onClick = { showResolveDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor)) {
                    Text("Cancel", color = textColor)
                }
            },
            containerColor = primaryColor
        )
    }

    // --- 3. DELETE USER DIALOG (NEW) ---
    if (showDeleteUserDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteUserDialog = false },
            title = { Text("Delete User Account", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Are you sure you want to delete this user?", color = textLightColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This will remove their data from the database. A notification will be sent before deletion.", color = errorColor, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        product?.listedBy?.let { userId ->
                            // CALL VIEWMODEL TO DELETE USER + NOTIFY
                            adminFlagViewModel.deleteUserAccount(userId)

                            Toast.makeText(context, "User Account Deleted", Toast.LENGTH_SHORT).show()
                            activity?.finish()
                        }
                        showDeleteUserDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor)
                ) {
                    Text("DELETE USER", color = textColor)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteUserDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor)) {
                    Text("Cancel", color = textColor)
                }
            },
            containerColor = primaryColor
        )
    }
}

// Keep the Helper Composable
@Composable
fun FlagInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, iconColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp).padding(top = 2.dp), tint = iconColor)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, color = textLightColor, fontSize = 12.sp)
            Text(text = value, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}