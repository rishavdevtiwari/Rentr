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
import com.example.rentr.viewmodel.AdminFlagViewModel
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

    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val adminFlagViewModel: AdminFlagViewModel = viewModel()

    val product by productViewModel.product.observeAsState()
    var sellerInfo by remember { mutableStateOf<com.example.rentr.model.UserModel?>(null) }
    var flaggerInfo by remember { mutableStateOf<com.example.rentr.model.UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog States
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResolveDialog by remember { mutableStateOf(false) }
    var showMarkDialog by remember { mutableStateOf(false) }
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
                title = { Text("Flag Review", color = textColor, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryColor)
            )
        },
        containerColor = primaryColor
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = accentColor) }
        } else if (product == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Product not found", color = textColor) }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Status Badge
                Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Box(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (getProductStatus()) {
                                    "APPEALED" -> infoColor
                                    "RESOLVED" -> successColor
                                    else -> warningColor
                                }.copy(alpha = 0.9f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(getProductStatus(), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(Modifier.fillMaxWidth().padding(16.dp)) {

                    // --- SELLER INFO (With Delete User Button) ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Person, null, tint = errorColor, modifier = Modifier.size(20.dp))
                                    Text("Seller Info", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { showDeleteUserDialog = true }) {
                                    Icon(Icons.Default.PersonRemove, "Delete User", tint = errorColor)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            sellerInfo?.let { seller ->
                                FlagInfoRow(Icons.Default.Person, "Name", seller.fullName, textLightColor)
                                FlagInfoRow(Icons.Default.Email, "Email", seller.email, textLightColor)
                                FlagInfoRow(Icons.Default.Flag, "Flags", seller.flagCount.toString(), textLightColor)
                            } ?: Text("Loading...", color = textLightColor)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // --- PRODUCT DETAILS ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.ShoppingBag, null, tint = warningColor, modifier = Modifier.size(20.dp))
                                Text("Product Details", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(12.dp))
                            product?.imageUrl?.firstOrNull()?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.rentrimage)
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                            Text(product?.title ?: "", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(product?.description ?: "", color = textLightColor, fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // --- FLAG DETAILS ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Flag Reason:", color = textColor, fontWeight = FontWeight.Medium)
                            Text(product?.flaggedReason?.joinToString(", ") ?: "None", color = textLightColor, fontSize = 13.sp)

                            if (product?.appealReason?.isNotEmpty() == true) {
                                Spacer(Modifier.height(12.dp))
                                Text("Appeal:", color = Color.Cyan, fontWeight = FontWeight.Medium)
                                Text(product!!.appealReason, color = Color.Cyan.copy(alpha = 0.8f), fontStyle = FontStyle.Italic)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- ACTION BUTTONS (Delete, Resolve, Mark) ---
                    if (product?.flagged == true) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null, Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Delete")
                                }
                                Button(
                                    onClick = { showResolveDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = successColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Resolve")
                                }
                            }

                            // MARK FOR REVIEW BUTTON
                            Button(
                                onClick = { showMarkDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = warningColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Flag, null, Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Mark for Review (Hide)")
                            }
                        }
                    } else {
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = successColor.copy(alpha = 0.1f))
                        ) {
                            Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, null, tint = successColor, modifier = Modifier.size(40.dp))
                                Text("Flag Resolved", color = textColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. DELETE PRODUCT
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Product?", color = textColor) },
            text = { Text("This is permanent. User will be notified.", color = textLightColor) },
            confirmButton = {
                Button(onClick = {
                    product?.let { adminFlagViewModel.deleteProduct(it); activity?.finish() }
                }, colors = ButtonDefaults.buttonColors(containerColor = errorColor)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
            containerColor = primaryColor
        )
    }

    // 2. RESOLVE FLAG
    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = { Text("Resolve Flag?", color = textColor) },
            text = { Text("Product will be visible again.", color = textLightColor) },
            confirmButton = {
                Button(onClick = {
                    product?.let { adminFlagViewModel.resolveFlag(it); activity?.finish() }
                }, colors = ButtonDefaults.buttonColors(containerColor = successColor)) { Text("Resolve") }
            },
            dismissButton = { TextButton(onClick = { showResolveDialog = false }) { Text("Cancel") } },
            containerColor = primaryColor
        )
    }

    // 3. MARK FOR REVIEW
    if (showMarkDialog) {
        AlertDialog(
            onDismissRequest = { showMarkDialog = false },
            title = { Text("Mark for Review?", color = textColor) },
            text = { Text("This will hide the product from listings. No notification will be sent.", color = textLightColor) },
            confirmButton = {
                Button(onClick = {
                    product?.let { adminFlagViewModel.markForReview(it); activity?.finish() }
                }, colors = ButtonDefaults.buttonColors(containerColor = warningColor)) { Text("Mark (Hide)") }
            },
            dismissButton = { TextButton(onClick = { showMarkDialog = false }) { Text("Cancel") } },
            containerColor = primaryColor
        )
    }

    // 4. DELETE USER
    if (showDeleteUserDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteUserDialog = false },
            title = { Text("Delete User Account?", color = textColor) },
            text = { Text("This will delete the user's data permanently. A notification will be sent.", color = errorColor) },
            confirmButton = {
                Button(onClick = {
                    product?.listedBy?.let { adminFlagViewModel.deleteUserAccount(it); activity?.finish() }
                }, colors = ButtonDefaults.buttonColors(containerColor = errorColor)) { Text("Delete User") }
            },
            dismissButton = { TextButton(onClick = { showDeleteUserDialog = false }) { Text("Cancel") } },
            containerColor = primaryColor
        )
    }
}

@Composable
fun FlagInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, color = color, fontSize = 12.sp)
            Text(value, color = textColor, fontSize = 14.sp)
        }
    }
}