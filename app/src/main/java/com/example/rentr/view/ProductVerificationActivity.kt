package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

// Color palette for admin verification
private val primaryColor = Color(0xFF1E1E1E)
private val accentColor = Color(0xFFFF6200)
private val successColor = Color(0xFF4CAF50)
private val errorColor = Color(0xFFF44336)
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
private val cardBackgroundColor = Color(0xFF2C2C2E)

class ProductVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sample data for preview - backend logic to implement later
        val productId = intent.getStringExtra("productId") ?: "sample_product_123"
        val productImg = intent.getIntExtra("productImg", R.drawable.bicycle)

        setContent {
            ProductVerificationScreen(productId = productId, productImg = productImg)
        }
    }
}

// Data class for demo purposes - backend logic to implement later
data class DemoProduct(
    val title: String = "Mountain Bike",
    val category: String = "Sports & Outdoors",
    val price: Double = 500.0,
    val quantity: Int = 3,
    val location: String = "Kathmandu, Nepal",
    val description: String = "Premium mountain bike with 21 gears, suspension forks, and durable aluminum frame. Perfect for off-road trails and mountain adventures.",
    val listedBy: String = "user_123",
    val status: String = "pending", // pending, verified, rejected
    val verificationNotes: String? = null,
    val imageUrls: List<Int> = listOf(R.drawable.bicycle) // Default to single image
)

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProductVerificationScreen(productId: String, productImg: Int) {
    val context = LocalContext.current
    val activity = context as? Activity

    // 1. Single image: imageUrls = listOf(R.drawable.bicycle)
    // 2. Multiple images: imageUrls = listOf(R.drawable.bicycle, R.drawable.bicycle, R.drawable.bicycle)
    val product = remember {
        DemoProduct(
            title = "Mountain Bike Pro",
            category = "Sports Equipment",
            price = 750.0,
            //quantity = 2,
            location = "Pokhara, Lakeside",
            description = "Professional grade mountain bike with advanced suspension system. Recently serviced and in excellent condition.",
            listedBy = "seller_456",
            status = "pending",
            imageUrls = listOf(R.drawable.bicycle) // Single image by default
        )
    }

    // Demo seller info - backend logic to implement later
    val sellerName = remember { "John Smith" }
    val sellerContact = remember { "+977 9841234567" }

    var isLoading by remember { mutableStateOf(false) }

    // State for image slider (only used when multiple images)
    val pagerState = rememberPagerState()

    // State for rejection dialog
    var showRejectionDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ProductVerificationTopBar(onBackClick = { activity?.finish() })
        },
        containerColor = primaryColor
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accentColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Product Image Section - Conditional rendering based on image count
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    if (product.imageUrls.size > 1) {
                        // Multiple Images: Show slider
                        HorizontalPager(
                            count = product.imageUrls.size,
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            Image(
                                painter = painterResource(id = product.imageUrls[page]),
                                contentDescription = "Product Image ${page + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Image indicator for multiple images
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        ) {
                            HorizontalPagerIndicator(
                                pagerState = pagerState,
                                modifier = Modifier.padding(8.dp),
                                activeColor = accentColor,
                                inactiveColor = textLightColor.copy(alpha = 0.5f)
                            )
                        }

                        // Image counter for multiple images
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${product.imageUrls.size}",
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // Single Image: Show static image without slider
                        Image(
                            painter = painterResource(id = product.imageUrls.firstOrNull() ?: productImg),
                            contentDescription = product.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // No indicators or counters for single image
                    }

                    // Status badge (always shown)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (product.status) {
                                    "verified" -> successColor.copy(alpha = 0.9f)
                                    "rejected" -> errorColor.copy(alpha = 0.9f)
                                    else -> accentColor.copy(alpha = 0.9f)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (product.status) {
                                "verified" -> "VERIFIED"
                                "rejected" -> "REJECTED"
                                else -> "PENDING REVIEW"
                            },
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Product Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title and Category
                    Text(
                        text = product.title,
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Image count indicator (only show if multiple images)
                    if (product.imageUrls.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${product.imageUrls.size} images",
                                color = textLightColor,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Basic Info Section
                            InfoRow(
                                icon = Icons.Default.Description,
                                label = "Category",
                                value = product.category,
                                iconColor = accentColor
                            )

                            InfoRow(
                                icon = Icons.Default.PriceCheck,
                                label = "Daily Price",
                                value = "NPR. ${"%.2f".format(product.price)}",
                                iconColor = accentColor
                            )

                            //InfoRow(
                              //  icon = Icons.Default.Description,
                                //label = "Quantity",
                                //value = product.quantity.toString(),
                                //iconColor = accentColor
                            //)

                            InfoRow(
                                icon = Icons.Default.LocationOn,
                                label = "Location",
                                value = product.location,
                                iconColor = accentColor
                            )

                            // Image count info
                            InfoRow(
                                icon = Icons.Default.Description,
                                label = "Images",
                                value = "${product.imageUrls.size} ${if (product.imageUrls.size == 1) "image" else "images"}",
                                iconColor = accentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Product Description",
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = product.description,
                                color = textLightColor,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seller Information Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Seller Information",
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            InfoRow(
                                icon = Icons.Default.Person,
                                label = "Name",
                                value = sellerName,
                                iconColor = accentColor
                            )

                            InfoRow(
                                icon = Icons.Default.Person,
                                label = "Contact",
                                value = sellerContact,
                                iconColor = accentColor
                            )

                            InfoRow(
                                icon = Icons.Default.Person,
                                label = "User ID",
                                value = product.listedBy,
                                iconColor = accentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Verification Notes (if any)
                    if (!product.verificationNotes.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Verification Notes",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = product.verificationNotes ?: "",
                                    color = textLightColor,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Action Buttons (only show if pending review)
                    if (product.status != "verified" && product.status != "rejected") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Reject Button
                            Button(
                                onClick = {
                                    showRejectionDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = errorColor)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Reject",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Reject",
                                        color = textColor,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            // Accept Button
                            Button(
                                onClick = {
                                    // Backend logic to implement later
                                    // Handle product acceptance
                                },
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
                                        contentDescription = "Accept",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Accept",
                                        color = textColor,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Show status message if already verified/rejected
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when (product.status) {
                                    "verified" -> successColor.copy(alpha = 0.1f)
                                    "rejected" -> errorColor.copy(alpha = 0.1f)
                                    else -> cardBackgroundColor
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = when (product.status) {
                                        "verified" -> Icons.Default.CheckCircle
                                        else -> Icons.Default.Close
                                    },
                                    contentDescription = "Status",
                                    tint = when (product.status) {
                                        "verified" -> successColor
                                        else -> errorColor
                                    },
                                    modifier = Modifier.size(40.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = when (product.status) {
                                        "verified" -> "Product Already Verified"
                                        else -> "Product Already Rejected"
                                    },
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = when (product.status) {
                                        "verified" -> "This product has been approved for listing"
                                        else -> "This product has been rejected from listing"
                                    },
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

        // Rejection Reason Dialog
        if (showRejectionDialog) {
            AlertDialog(
                onDismissRequest = { showRejectionDialog = false },
                title = {
                    Text(
                        text = "Reason for Rejection",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Please provide a reason for rejecting this product listing:",
                            color = textLightColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = rejectionReason,
                            onValueChange = { rejectionReason = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = {
                                Text(
                                    text = "Enter rejection reason...",
                                    color = textLightColor.copy(alpha = 0.7f)
                                )
                            },
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = cardBackgroundColor,
                                unfocusedContainerColor = cardBackgroundColor,
                                disabledContainerColor = cardBackgroundColor,
                                focusedIndicatorColor = accentColor,
                                unfocusedIndicatorColor = textLightColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = false,
                            maxLines = 5
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Common reasons: Poor quality images, incomplete information, prohibited item, suspicious listing, etc.",
                            color = textLightColor,
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
                                showRejectionDialog = false
                                rejectionReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardBackgroundColor
                            )
                        ) {
                            Text("Cancel", color = textColor)
                        }
                        Button(
                            onClick = {
                                // Backend logic to implement later
                                // Submit rejection with reason: rejectionReason
                                showRejectionDialog = false
                                // Reset reason
                                rejectionReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = errorColor
                            )
                        ) {
                            Text("Submit Rejection", color = textColor)
                        }
                    }
                },
                containerColor = primaryColor,
                titleContentColor = textColor,
                textContentColor = textLightColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductVerificationTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Product Verification",
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
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
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, iconColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier
                .size(20.dp)
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

// Preview for Single Image
@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E, name = "Single Image")
@Composable
fun ProductVerificationScreenPreview() {
    ProductVerificationScreen("sample_product_123", R.drawable.bicycle)
}
