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
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImp1
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
    val userViewModel = remember { UserViewModel(UserRepoImp1()) }

    val product by productViewModel.product.observeAsState()
    var sellerInfo by remember { mutableStateOf<com.example.rentr.model.UserModel?>(null) }
    var flaggerInfo by remember { mutableStateOf<com.example.rentr.model.UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResolveDialog by remember { mutableStateOf(false) }
    var showMarkDialog by remember { mutableStateOf(false) }

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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = errorColor,
                        modifier = Modifier.size(64.dp)
                    )
                    Text("Product not found", color = textColor, fontSize = 18.sp)
                    Button(
                        onClick = { activity?.finish() },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
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
                        Text(
                            text = getProductStatus(),
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Seller",
                                    tint = errorColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Seller Information",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            sellerInfo?.let { seller ->
                                FlagInfoRow(
                                    icon = Icons.Default.Person,
                                    label = "Name",
                                    value = seller.fullName,
                                    iconColor = textLightColor
                                )

                                FlagInfoRow(
                                    icon = Icons.Default.Email,
                                    label = "Email",
                                    value = seller.email,
                                    iconColor = textLightColor
                                )

                                FlagInfoRow(
                                    icon = Icons.Default.Flag,
                                    label = "Flag Count",
                                    value = seller.flagCount.toString(),
                                    iconColor = textLightColor
                                )

                                if (seller.phoneNumber.isNotEmpty()) {
                                    FlagInfoRow(
                                        icon = Icons.Default.Phone,
                                        label = "Phone",
                                        value = seller.phoneNumber,
                                        iconColor = textLightColor
                                    )
                                }
                            } ?: run {
                                Text("Loading seller info...", color = textLightColor)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.ShoppingBag,
                                    contentDescription = "Product",
                                    tint = warningColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Product Details",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            product?.imageUrl?.firstOrNull()?.let { imageUrl ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
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

                            Text(
                                text = product?.title ?: "",
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = product?.description ?: "",
                                color = textLightColor,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = product?.category ?: "",
                                    color = textLightColor,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "NPR. ${"%.2f".format(product?.price ?: 0.0)}",
                                    color = accentColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Flagged by ${product?.flaggedBy?.size ?: 0} user(s)",
                                color = Color.Red.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = "Flag Details",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Flag Details",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Reason for Flagging:",
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            if (product?.flagReason?.isNotEmpty() == true) {
                                val flagReasonText = if (product!!.flagReason is List<*>) {
                                    product!!.flagReason.joinToString(", ")
                                } else {
                                    product!!.flagReason.toString()
                                }
                                Text(
                                    text = flagReasonText,
                                    color = textLightColor,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            } else {
                                Text(
                                    text = "No reason provided",
                                    color = textLightColor,
                                    fontSize = 13.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }

                            if (product?.appealReason?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Seller Appeal:",
                                    color = Color.Cyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                val appealReasonText = if (product!!.appealReason is List<*>) {
                                    product!!.appealReason.joinToString(", ")
                                } else {
                                    product!!.appealReason.toString()
                                }
                                Text(
                                    text = appealReasonText,
                                    color = Color.Cyan.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }

                    flaggerInfo?.let {
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Report,
                                        contentDescription = "Reporter",
                                        tint = infoColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Reported By (First User)",
                                        color = textColor,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                FlagInfoRow(
                                    icon = Icons.Default.Person,
                                    label = "Name",
                                    value = it.fullName,
                                    iconColor = textLightColor
                                )

                                FlagInfoRow(
                                    icon = Icons.Default.Email,
                                    label = "Email",
                                    value = it.email,
                                    iconColor = textLightColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (product?.flagged == true) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = errorColor)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Delete",
                                            color = textColor,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showResolveDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = successColor)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Resolve",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Resolve",
                                            color = textColor,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { showMarkDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = warningColor)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Flag,
                                        contentDescription = "Mark",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mark for Review",
                                        color = textColor,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = successColor.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Resolved",
                                    tint = successColor,
                                    modifier = Modifier.size(40.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Flag Already Resolved",
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "This product has been reviewed and is now available for rent.",
                                    color = textLightColor,
                                    fontSize = 14.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Product",
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "This will PERMANENTLY DELETE the product from the platform.",
                        color = textLightColor,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Seller's flag count will remain the same.",
                        color = textLightColor,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action cannot be undone!",
                        color = errorColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showDeleteDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cardBackgroundColor
                        )
                    ) {
                        Text("Cancel", color = textColor)
                    }
                    Button(
                        onClick = {
                            product?.let { currentProduct ->
                                productViewModel.deleteProduct(currentProduct.productId) { success, _ ->
                                    if (success) {
                                        Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show()
                                        activity?.finish()
                                    }
                                }
                            }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = errorColor
                        )
                    ) {
                        Text("DELETE PERMANENTLY", color = textColor)
                    }
                }
            },
            containerColor = primaryColor
        )
    }

    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = {
                Text(
                    text = "Resolve Flag",
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Resolving will clear the flag and make the product available again.",
                        color = textLightColor,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The flag will be removed from product and seller's flag count will decrease.",
                        color = textLightColor,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showResolveDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cardBackgroundColor
                        )
                    ) {
                        Text("Cancel", color = textColor)
                    }
                    Button(
                        onClick = {
                            product?.let { currentProduct ->
                                val updatedProduct = currentProduct.copy(
                                    flagged = false,
                                    flaggedBy = emptyList(),
                                    flagReason = emptyList(),
                                    appealReason = emptyList(),
                                    availability = true
                                )
                                productViewModel.updateProduct(currentProduct.productId, updatedProduct) { success, _ ->
                                    if (success) {
                                        sellerInfo?.let { seller ->
                                            val newFlagCount = if (seller.flagCount > 0) seller.flagCount - 1 else 0
                                            val updatedSeller = seller.copy(flagCount = newFlagCount)
                                            userViewModel.updateProfile(seller.email, updatedSeller) { _, _ ->
                                                Toast.makeText(context, "Flag resolved", Toast.LENGTH_SHORT).show()
                                                activity?.finish()
                                            }
                                        }
                                    }
                                }
                            }
                            showResolveDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = successColor
                        )
                    ) {
                        Text("Confirm Resolve", color = textColor)
                    }
                }
            },
            containerColor = primaryColor
        )
    }

    if (showMarkDialog) {
        AlertDialog(
            onDismissRequest = { showMarkDialog = false },
            title = {
                Text(
                    text = "Mark for Review",
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Marking will hide the product from listings. Seller can appeal this decision.",
                        color = textLightColor,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Product will be hidden until further review.",
                        color = textLightColor,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showMarkDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cardBackgroundColor
                        )
                    ) {
                        Text("Cancel", color = textColor)
                    }
                    Button(
                        onClick = {
                            product?.let { currentProduct ->
                                val updatedProduct = currentProduct.copy(
                                    availability = false,
                                    flagged = true
                                )
                                productViewModel.updateProduct(currentProduct.productId, updatedProduct) { success, _ ->
                                    if (success) {
                                        Toast.makeText(context, "Product marked for review", Toast.LENGTH_SHORT).show()
                                        activity?.finish()
                                    }
                                }
                            }
                            showMarkDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = warningColor
                        )
                    ) {
                        Text("Mark for Review", color = textColor)
                    }
                }
            },
            containerColor = primaryColor
        )
    }
}

@Composable
fun FlagInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, iconColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                color = textLightColor,
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}