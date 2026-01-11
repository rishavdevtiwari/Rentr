@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rentr.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ListedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListedScreen()
        }
    }
}

@Composable
fun ListedScreen() {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val products by productViewModel.allProducts.observeAsState(emptyList())
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Available / Unavailable", "Ongoing", "Rented Out", "Flagged")

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val uId = userViewModel.getCurrentUser()?.uid
    var isUserVerified by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductModel?>(null) }

    var showAppealDialog by remember { mutableStateOf(false) }
    var productToAppeal by remember { mutableStateOf<ProductModel?>(null) }
    var appealReason by remember { mutableStateOf("") }

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun refreshData() {
        coroutineScope.launch {
            isRefreshing = true
            uId?.let {
                productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> }
                userViewModel.getUserById(it) { success, _, user ->
                    if (success) user?.let { fetchedUser -> isUserVerified = fetchedUser.verified }
                }
            }
            delay(1000)
            isRefreshing = false
        }
    }

    LaunchedEffect(uId) {
        uId?.let {
            productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> }

            userViewModel.getUserById(it) { success, msg, user ->
                if (success) {
                    user?.let { fetchedUser ->
                        isUserVerified = fetchedUser.verified
                    }
                }
            }
        }
    }

    val newListingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            uId?.let { productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> } }
        }
    }

    val editListingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            uId?.let { productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> } }
        }
    }

    val filteredList = when (selectedTabIndex) {
        0 -> products.filter { it.verified && it.rentalStatus == "" && !it.outOfStock }
        1 -> products.filter { (it.rentalStatus == "pending" || it.rentalStatus == "approved") && !it.outOfStock }
        2 -> products.filter { it.outOfStock }
        3 -> products.filter { it.flagged && it.flaggedBy.isNotEmpty() }
        else -> emptyList()
    }

    if (showDeleteDialog && productToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion", color = Color.White) },
            text = {
                Column {
                    Text("Are you sure you want to delete '${productToDelete?.title}'?", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (productToDelete?.flagged == true) {
                        Text(
                            "⚠️ This product is flagged. Deleting it will NOT reduce your flag count.",
                            color = Color.Yellow,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { product ->
                            productViewModel.deleteProduct(product.productId) { success, msg ->
                                if (success) {
                                    Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show()
                                    uId?.let { productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> } }
                                } else {
                                    Toast.makeText(context, "Failed to delete: $msg", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        showDeleteDialog = false
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Yes, Delete") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    productToDelete = null
                }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    if (showAppealDialog && productToAppeal != null) {
        AlertDialog(
            onDismissRequest = {
                showAppealDialog = false
                appealReason = ""
                productToAppeal = null
            },
            title = { Text("Submit Appeal", color = Color.White) },
            text = {
                Column {
                    Text("Appeal for: ${productToAppeal?.title}", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    val flaggedReasons = productToAppeal?.flaggedReason?.joinToString(", ")
                    if (!flaggedReasons.isNullOrEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Flagged Reasons:", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(flaggedReasons, color = Color.Red.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text("Explain why this flag should be removed:", color = Color.LightGray, fontSize = 14.sp)
                    OutlinedTextField(
                        value = appealReason,
                        onValueChange = { appealReason = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Enter your appeal reason...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Field,
                            unfocusedContainerColor = Field,
                            focusedBorderColor = Orange,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = false,
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚠️ Appeal will be reviewed by admin. Product remains hidden until review.",
                        color = Color.Yellow,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (appealReason.trim().isEmpty()) {
                            Toast.makeText(context, "Please enter appeal reason", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        productToAppeal?.let { product ->
                            val updatedProduct = product.copy(
                                appealReason = appealReason.trim(),
                                flagged = true,
                                availability = false
                            )
                            productViewModel.updateProduct(product.productId, updatedProduct) { success, msg ->
                                if (success) {
                                    uId?.let { productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> } }
                                    Toast.makeText(context, "Appeal submitted for admin review", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to submit appeal: $msg", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        showAppealDialog = false
                        appealReason = ""
                        productToAppeal = null
                    },
                    enabled = appealReason.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) { Text("Submit Appeal") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAppealDialog = false
                        appealReason = ""
                        productToAppeal = null
                    }
                ) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = { ListedTopAppBar() },
        floatingActionButton = {
            if (selectedTabIndex != 3) {
                FloatingActionButton(
                    onClick = {
                        if (!isUserVerified) {
                            Toast.makeText(context, "This feature is locked for you. You are not verified yet.", Toast.LENGTH_LONG).show()
                        } else {
                            val intent = Intent(context, NewListingActivity::class.java)
                            newListingLauncher.launch(intent)
                        }
                    },
                    containerColor = Orange,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.Black)
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { refreshData() },
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                height = 3.dp,
                                color = Orange
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) Color.White else Color.Gray
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(top = 20.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(filteredList) { product ->
                        ListedItemCardCompact(
                            product = product,
                            isFlaggedTab = selectedTabIndex == 3,
                            isOngoingTab = selectedTabIndex == 1,
                            onEditClicked = {
                                if (product.flagged && product.flaggedBy.isNotEmpty()) {
                                    Toast.makeText(context, "Cannot edit flagged item. Submit appeal first.", Toast.LENGTH_SHORT).show()
                                } else {
                                    val intent = Intent(context, EditListedActivity::class.java)
                                    intent.putExtra("productId", product.productId)
                                    editListingLauncher.launch(intent)
                                }
                            },
                            onDeleteClicked = {
                                if (product.flagged && product.flaggedBy.isNotEmpty()) {
                                    Toast.makeText(context, "Cannot delete flagged item. Submit appeal first.", Toast.LENGTH_SHORT).show()
                                } else {
                                    productToDelete = product
                                    showDeleteDialog = true
                                }
                            },
                            onAppealClicked = {
                                productToAppeal = product
                                showAppealDialog = true
                            },
                            onAcceptClicked = {
                                val updatedProduct = product.copy(
                                    rentalStatus = "approved",
                                    rentalStartDate = System.currentTimeMillis()
                                )
                                productViewModel.updateProduct(product.productId, updatedProduct) { success, msg ->
                                    if (success) {
                                        uId?.let { productViewModel.getAllProductsByUser(it) { _, _, _ -> } }
                                        Toast.makeText(context, "Rental accepted", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to accept: $msg", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onRejectClicked = {
                                val updatedProduct = product.copy(
                                    rentalStatus = "",
                                    rentalRequesterId = "",
                                    rentalDays = 1
                                )
                                productViewModel.updateProduct(product.productId, updatedProduct) { success, msg ->
                                    if (success) {
                                        uId?.let { productViewModel.getAllProductsByUser(it) { _, _, _ -> } }
                                        Toast.makeText(context, "Rental rejected", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to reject: $msg", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }

                    if (filteredList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = when (selectedTabIndex) {
                                            0 -> Icons.Default.Inventory
                                            1 -> Icons.Default.Schedule
                                            2 -> Icons.Default.ShoppingBag
                                            3 -> Icons.Default.Flag
                                            else -> Icons.Default.Inventory
                                        },
                                        contentDescription = "Empty",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = when (selectedTabIndex) {
                                            0 -> "No available/unavailable items"
                                            1 -> "No ongoing rentals"
                                            2 -> "No rented out items"
                                            3 -> "No flagged items"
                                            else -> "No items"
                                        },
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListedItemCardCompact(
    product: ProductModel,
    isFlaggedTab: Boolean,
    isOngoingTab: Boolean,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onAppealClicked: () -> Unit,
    onAcceptClicked: () -> Unit,
    onRejectClicked: () -> Unit
) {
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    var requesterName by remember { mutableStateOf<String?>(null) }
    val isFlagged = product.flagged && product.flaggedBy.isNotEmpty()
    val hasAppeal = product.appealReason.isNotEmpty()

    LaunchedEffect(product.rentalRequesterId) {
        if (isOngoingTab && product.rentalRequesterId.isNotEmpty()) {
            userViewModel.getUserById(product.rentalRequesterId) { success, _, user ->
                if (success && user != null) requesterName = user.fullName
            }
        }
    }

    val status = when {
        product.rentalStatus == "approved" -> "Waiting for Payment"
        product.rentalStatus == "pending" -> "Pending Request"
        isFlagged -> "FLAGGED"
        product.outOfStock -> "Rented Out"
        !product.availability -> "Unavailable"
        else -> "Available"
    }

    val isRented = status == "Rented Out"
    val isUnavailable = status == "Unavailable"
    val isPending = status == "Pending Request"
    val isWaitingForPayment = status == "Waiting for Payment"
    val imageUrl = product.imageUrl.firstOrNull()

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.height(if (isFlaggedTab || isOngoingTab) 130.dp else 110.dp).width(320.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFlagged -> Color.Red.copy(alpha = 0.1f)
                isRented -> Field.copy(alpha = 0.6f)
                isUnavailable -> Color.Gray.copy(alpha = 0.4f)
                isPending || isWaitingForPayment -> Color.Blue.copy(alpha = 0.2f)
                else -> Field
            }
        ),
        border = when {
            isFlagged -> BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
            hasAppeal -> BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.3f))
            else -> null
        }
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl,
                placeholder = painterResource(R.drawable.rentrimage),
                error = painterResource(R.drawable.rentrimage),
                contentDescription = product.title,
                modifier = Modifier.size(if (isFlaggedTab || isOngoingTab) 90.dp else 80.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(product.title, color = if (isUnavailable) Color.LightGray else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Box(
                    modifier = Modifier.background(
                        when {
                            isFlagged -> Color.Red.copy(alpha = 0.2f)
                            isRented -> Color.Red.copy(alpha = 0.6f)
                            isUnavailable -> Color.Gray.copy(alpha = 0.4f)
                            isPending || isWaitingForPayment -> Color.Blue.copy(alpha = 0.3f)
                            else -> Orange.copy(alpha = 0.2f)
                        },
                        RoundedCornerShape(6.dp)
                    ).padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (hasAppeal && isFlagged) "APPEAL PENDING" else status,
                        color = when {
                            isFlagged && !hasAppeal -> Color.Red
                            hasAppeal && isFlagged -> Color.Cyan
                            isRented -> Color.White
                            isUnavailable -> Color.LightGray
                            isPending || isWaitingForPayment -> Color.White
                            else -> Orange
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (isOngoingTab && requesterName != null) Text("Requested by: $requesterName", color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
                if (isFlagged && !isFlaggedTab) Text("Flagged by ${product.flaggedBy.size} user(s)", color = Color.Red.copy(alpha = 0.8f), fontSize = 10.sp)
                if (hasAppeal) Text("✓ Appeal submitted", color = Color.Cyan, fontSize = 10.sp)
                Text("NPR. ${String.format("%.2f", product.price)}/day", color = if (isUnavailable) Color.LightGray else Color.White, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
                if (isFlaggedTab) {
                    Button(onClick = onAppealClicked, enabled = !hasAppeal, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = if (hasAppeal) Color.Gray else Orange, contentColor = Color.Black), modifier = Modifier.height(35.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, "Appeal", Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (hasAppeal) "Appealed" else "Appeal", fontSize = 10.sp)
                        }
                    }
                } else if (isOngoingTab) {
                    if (isPending) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = onRejectClicked, modifier = Modifier.size(32.dp).background(Color.Red.copy(alpha = 0.2f), CircleShape)) { Icon(Icons.Default.Close, "Reject", tint = Color.Red) }
                            IconButton(onClick = onAcceptClicked, modifier = Modifier.size(32.dp).background(Color(0xFF4CAF50).copy(alpha = 0.2f), CircleShape)) { Icon(Icons.Default.Check, "Accept", tint = Color(0xFF4CAF50)) }
                        }
                    }
                } else {
                    if (isFlagged) {
                        Button(onClick = onAppealClicked, enabled = !hasAppeal, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = if (hasAppeal) Color.Gray else Orange, contentColor = Color.Black), modifier = Modifier.height(30.dp)) {
                            Text(if (hasAppeal) "Appealed" else "Appeal", fontSize = 10.sp)
                        }
                    } else {
                        Row {
                            IconButton(onClick = onEditClicked, modifier = Modifier.size(24.dp), enabled = !isRented && !isUnavailable) { Icon(Icons.Default.Edit, "Edit", tint = if (isRented || isUnavailable) Color.DarkGray else Color.LightGray) }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onDeleteClicked, modifier = Modifier.size(24.dp), enabled = !isRented && !isUnavailable) { Icon(Icons.Default.Delete, "Delete", tint = if (isRented || isUnavailable) Color.DarkGray else Color.Red) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListedTopAppBar() {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 30.dp, start = 16.dp, end = 16.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Text("My Listings", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(20.dp))
}
