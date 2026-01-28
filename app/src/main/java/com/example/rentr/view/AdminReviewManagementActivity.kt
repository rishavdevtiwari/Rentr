package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rentr.model.ProductModel
import com.example.rentr.model.UserModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel

private val Orange = Color(0xFFFF5D18)
val Field = Color(0xFF333232)
private val Outline = Color(0xFF818181)

enum class FlagCategory(val label: String, val color: Color) {
    ALL("All", Color(0xFF4CAF50)),
    PENDING("Pending Review", Color(0xFFFF9800)),
    UNDER_REVIEW("Under Review", Color(0xFFFFC107)),
    APPEALED("Appealed", Color(0xFF2196F3)),
    RESOLVED("Resolved", Color(0xFF4CAF50)),
    FLAGGED_USERS("Flagged Users", Color(0xFF9C27B0))
}

class AdminReviewManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReviewScreenContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreenContent(
    productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.Factory(ProductRepoImpl())
    ),
    userViewModel: UserViewModel = viewModel(
        factory = UserViewModel.Factory(UserRepoImpl())
    )
) {
    val context = LocalContext.current
    val products by productViewModel.allProducts.observeAsState(emptyList())
    val usersMap by userViewModel.allUsersMap.observeAsState(emptyMap())

    var selectedCategory by remember { mutableStateOf(FlagCategory.ALL) }

    LaunchedEffect(Unit) {
        productViewModel.getAllProducts { _, _, _ -> }
        userViewModel.getAllUsers { _, _, _ -> }

        productViewModel.getFlaggedProducts { _, _, _ -> }
    }

    val usersList = remember(usersMap) {
        usersMap.values.toList()
    }

    val flaggedUsers = remember(usersList) {
        usersList.filter { it.flagCount > 0 }.sortedByDescending { it.flagCount }
    }

    val filteredProducts = remember(products, selectedCategory) {
        when (selectedCategory) {
            FlagCategory.ALL -> products.filter {
                it.flaggedBy.isNotEmpty() || it.flagged
            }
            FlagCategory.PENDING -> products.filter {
                // Products with user flags but not yet marked by admin
                it.flaggedBy.isNotEmpty() && !it.flagged
            }
            FlagCategory.UNDER_REVIEW -> products.filter {
                // Products marked for review by admin (flagged=true)
                it.flagged
            }
            FlagCategory.RESOLVED -> products.filter {
                // Products that were flagged but now resolved
                !it.flagged && it.flaggedBy.isEmpty()
            }
            FlagCategory.APPEALED -> products.filter {
                it.appealReason.isNotEmpty()
            }
            FlagCategory.FLAGGED_USERS -> emptyList()
        }
    }

    val categoryCounts = remember(products, flaggedUsers) {
        mapOf(
            FlagCategory.ALL to products.count { it.flaggedBy.isNotEmpty() || it.flagged },
            FlagCategory.PENDING to products.count {
                it.flaggedBy.isNotEmpty() && !it.flagged
            },
            FlagCategory.UNDER_REVIEW to products.count { it.flagged },
            FlagCategory.RESOLVED to products.count {
                !it.flagged && it.flaggedBy.isEmpty()
            },
            FlagCategory.APPEALED to products.count {
                it.appealReason.isNotEmpty()
            },
            FlagCategory.FLAGGED_USERS to flaggedUsers.size
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("ADMIN REVIEW", color = Orange, fontWeight = FontWeight.Black)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
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
        ) {
            CategoryTabs(
                selectedCategory = selectedCategory,
                onCategorySelected = { category -> selectedCategory = category },
                categoryCounts = categoryCounts
            )

            if (selectedCategory == FlagCategory.FLAGGED_USERS) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Flagged Users: ${flaggedUsers.size} users",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    if (flaggedUsers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(
                                                color = Color(0xFF9C27B0).copy(alpha = 0.1f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "👤",
                                            color = Color(0xFF9C27B0),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "No Flagged Users",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = "Users with flag counts > 0 will appear here",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        items(flaggedUsers) { user ->
                            FlaggedUserCard(user = user)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Text(
                            text = "${selectedCategory.label}: ${filteredProducts.size} items",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    if (filteredProducts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(
                                                color = selectedCategory.color.copy(alpha = 0.1f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "✓",
                                            color = selectedCategory.color,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = when (selectedCategory) {
                                            FlagCategory.ALL -> "No flagged items found"
                                            FlagCategory.PENDING -> "No pending items for review"
                                            FlagCategory.UNDER_REVIEW -> "No items under review"
                                            FlagCategory.RESOLVED -> "No resolved flags"
                                            FlagCategory.APPEALED -> "No appealed items"
                                            else -> "No items"
                                        },
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = when (selectedCategory) {
                                            FlagCategory.ALL -> "All flagged items will appear here"
                                            FlagCategory.PENDING -> "Items flagged by users awaiting admin review"
                                            FlagCategory.UNDER_REVIEW -> "Items marked for review by admin"
                                            FlagCategory.RESOLVED -> "Resolved flags will appear here"
                                            FlagCategory.APPEALED -> "Items with seller appeals will appear here"
                                            else -> ""
                                        },
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredProducts) { product ->
                            FlaggedProductCardReal(
                                product = product,
                                sellerName = usersMap[product.listedBy]?.fullName ?: "Unknown",
                                onClick = {
                                    val intent = Intent(context, AdminFlaggedReviewActivity::class.java)
                                    intent.putExtra("productId", product.productId)
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTabs(
    selectedCategory: FlagCategory,
    onCategorySelected: (FlagCategory) -> Unit,
    categoryCounts: Map<FlagCategory, Int>
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FlagCategory.values().forEach { category ->
                CategoryTab(
                    category = category,
                    isSelected = selectedCategory == category,
                    count = categoryCounts[category] ?: 0,
                    onClick = { onCategorySelected(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CategoryTab(
    category: FlagCategory,
    isSelected: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(
                color = if (isSelected) category.color.copy(alpha = 0.2f) else Field,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) category.color else Outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = category.label,
                color = if (isSelected) category.color else Color.White,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = category.color.copy(alpha = if (isSelected) 0.3f else 0.1f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = count.toString(),
                    color = if (isSelected) category.color else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FlaggedUserCard(
    user: UserModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.DarkGray, CircleShape)
                    .border(2.dp, Color(0xFF9C27B0), CircleShape)
            ) {
                if (user.profileImage.isNotEmpty()) {
                    AsyncImage(
                        model = user.profileImage,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.fullName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user.email,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (user.phoneNumber.isNotEmpty()) {
                    Text(
                        text = "Phone: ${user.phoneNumber}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(Color(0xFF9C27B0).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF9C27B0), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FLAGS",
                        color = Color(0xFF9C27B0),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = user.flagCount.toString(),
                        color = Color(0xFF9C27B0),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FlaggedProductCardReal(
    product: ProductModel,
    sellerName: String,
    onClick: () -> Unit
) {

    val status = when {
        product.flagged -> "UNDER REVIEW"  // Admin marked for review
        product.flaggedBy.isNotEmpty() -> "PENDING"  // User flags, not reviewed
        product.appealReason.isNotEmpty() -> "APPEALED"
        else -> "RESOLVED"
    }

    val statusColor = when (status) {
        "PENDING" -> Color(0xFFFF9800)
        "UNDER REVIEW" -> Color(0xFFFFC107)
        "RESOLVED" -> Color(0xFF4CAF50)
        "APPEALED" -> Color(0xFF2196F3)
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Field),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Seller: ", color = Color.LightGray, fontSize = 14.sp)
                Text(sellerName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (product.flaggedBy.isNotEmpty()) {
                Text(
                    text = "Flagged by ${product.flaggedBy.size} user(s)",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )

                if (product.flaggedReason.isNotEmpty()) {
                    val flagReasonText = product.flaggedReason.joinToString(", ")
                    Text(
                        text = "Reasons: ${flagReasonText.take(50)}${if (flagReasonText.length > 50) "..." else ""}",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (product.appealReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "⚖️ ",
                        color = Color.Cyan,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Appeal: ${product.appealReason.take(50)}${if (product.appealReason.length > 50) "..." else ""}",
                        color = Color.Cyan.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (product.flagged) "Hidden from listings" else "Visible to users",
                        color = if (product.flagged) Color.Red.copy(alpha = 0.8f) else Color.Green.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "NPR. ${product.price}",
                    color = Orange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = when (status) {
                        "RESOLVED" -> "View Details"
                        "APPEALED" -> "Review Appeal"
                        "PENDING" -> "Review Flags"
                        else -> "Manage Review"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}