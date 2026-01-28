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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel
import com.example.rentr.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ListedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ListedScreen() }
    }
}

@Composable
fun ListedScreen() {
    val context = LocalContext.current
    val productViewModel = remember {
        ViewModelProvider(
            context as ComponentActivity,
            ProductViewModel.Factory(ProductRepoImpl())
        ).get(ProductViewModel::class.java)
    }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val chatViewModel = remember { ChatViewModel() }

    val products by productViewModel.allProducts.observeAsState(emptyList())
    val uId = userViewModel.getCurrentUser()?.uid
    var isUserVerified by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }

    val tabs = listOf("Available", "Pending", "Active", "Flagged")
    val coroutineScope = rememberCoroutineScope()

    val editListingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            uId?.let { id -> productViewModel.getAllProductsByUser(userId = id) { _, _, _ -> } }
        }
    }

    val dialogState = remember { DialogState() }

    fun refreshData() = coroutineScope.launch {
        isRefreshing = true
        uId?.let {
            productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> }
            userViewModel.getUserById(it) { success, _, user ->
                if (success) isUserVerified = user?.verified ?: false
            }
        }
        delay(1000)
        isRefreshing = false
    }

    LaunchedEffect(uId) {
        uId?.let { id ->
            productViewModel.getAllProductsByUser(userId = id) { _, _, _ -> }
            userViewModel.getUserById(id) { success, _, user ->
                if (success) isUserVerified = user?.verified ?: false
            }
        }
    }

    val filteredList = when (selectedTabIndex) {
        0 -> products.filter {
            it.rentalStatus == "" && !it.outOfStock && !it.flagged && it.availability
        }
        1 -> products.filter {
            it.rentalStatus in listOf(
                ProductViewModel.STATUS_PENDING,
                ProductViewModel.STATUS_APPROVED,
                ProductViewModel.STATUS_PAID
            ) && !it.flagged
        }
        2 -> products.filter {
            it.rentalStatus in listOf(
                ProductViewModel.STATUS_RENTED,
                ProductViewModel.STATUS_RETURNING
            ) && !it.flagged
        }
        3 -> products.filter { it.flagged }
        else -> emptyList()
    }

    RenderDialogs(dialogState, context, productViewModel, uId, ::refreshData)

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Listings", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        floatingActionButton = {
            if (selectedTabIndex != 3) {
                FloatingActionButton(
                    onClick = {
                        if (!isUserVerified) {
                            Toast.makeText(context, "Account verification required", Toast.LENGTH_LONG).show()
                        } else {
                            val intent = Intent(context, NewListingActivity::class.java)
                            editListingLauncher.launch(intent)
                        }
                    },
                    containerColor = Orange,
                    shape = CircleShape
                ) { Icon(Icons.Default.Add, null, tint = Color.Black) }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { refreshData() },
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ScrollableTabs(tabs, selectedTabIndex) { selectedTabIndex = it }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filteredList.isEmpty()) {
                        item { ListedEmptyState(selectedTabIndex) }
                    } else {
                        items(filteredList) { product ->
                            ProductItemCard(
                                product = product,
                                isFlaggedTab = selectedTabIndex == 3,
                                productViewModel = productViewModel,
                                chatViewModel = chatViewModel,
                                uId = uId,
                                dialogState = dialogState,
                                editListingLauncher = editListingLauncher,
                                context = context,
                                refreshData = ::refreshData
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScrollableTabs(tabs: List<String>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    Surface(color = Color.Black, modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(tabs) { index, title ->
                val isSelected = selectedIndex == index
                Surface(
                    onClick = { onTabSelected(index) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Orange else Color.Transparent,
                    border = if (!isSelected) BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)) else null,
                    modifier = Modifier.height(40.dp).wrapContentWidth().padding(horizontal = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(Color.Black, RoundedCornerShape(2.dp))
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListedEmptyState(selectedTabIndex: Int) {
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
                    0 -> "No available items"
                    1 -> "No pending rentals"
                    2 -> "No active rentals"
                    3 -> "No flagged items"
                    else -> "No items"
                },
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ProductItemCard(
    product: ProductModel,
    isFlaggedTab: Boolean,
    productViewModel: ProductViewModel,
    chatViewModel: ChatViewModel,
    uId: String?,
    dialogState: DialogState,
    editListingLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    context: android.content.Context,
    refreshData: () -> Unit
) {
    ListedItemCard(
        product = product,
        isFlaggedTab = isFlaggedTab,
        dialogState = dialogState,
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
                dialogState.showDeleteDialog(product)
            }
        },
        onAppealClicked = { dialogState.showAppealDialog(product) },
        onAcceptClicked = {
            productViewModel.approveRentalRequest(product.productId) { success, msg ->
                if (success) {
                    Toast.makeText(context, "Rental accepted", Toast.LENGTH_SHORT).show()
                    refreshData()
                } else {
                    Toast.makeText(context, "Failed to accept: $msg", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onRejectClicked = {
            productViewModel.rejectRentalRequest(product.productId) { success, msg ->
                if (success) {
                    Toast.makeText(context, "Rental rejected", Toast.LENGTH_SHORT).show()
                    refreshData()
                } else {
                    Toast.makeText(context, "Failed to reject: $msg", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onHandoverClicked = { dialogState.showHandoverDialog(product) },
        onVerifyReturnClicked = { dialogState.showVerifyReturnDialog(product) },
        onChatClicked = {
            if (product.rentalRequesterId.isNotEmpty() && uId != null) {
                chatViewModel.startOrGetConversation(
                    productId = product.productId,
                    renterId = product.rentalRequesterId,
                    sellerId = uId,
                    initialMessage = null
                ) { chatId ->
                    if (chatId != null) {
                        val intent = Intent(context, ChatActivity::class.java).apply {
                            putExtra("CONVERSATION_ID", chatId)
                            putExtra("CHAT_TITLE", product.title)
                        }
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Failed to start chat", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Unable to start chat", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

class DialogState {
    var showDeleteDialog by mutableStateOf(false)
    var productToDelete by mutableStateOf<ProductModel?>(null)
    var showAppealDialog by mutableStateOf(false)
    var productToAppeal by mutableStateOf<ProductModel?>(null)
    var appealReason by mutableStateOf("")
    var showHandoverDialog by mutableStateOf(false)
    var productToHandover by mutableStateOf<ProductModel?>(null)
    var showVerifyReturnDialog by mutableStateOf(false)
    var productToVerifyReturn by mutableStateOf<ProductModel?>(null)
    var showCodPaymentDialog by mutableStateOf(false)
    var productToMarkPaid by mutableStateOf<ProductModel?>(null)

    fun showCodPaymentDialog(product: ProductModel) {
        productToMarkPaid = product
        showCodPaymentDialog = true
    }

    fun showDeleteDialog(product: ProductModel) {
        productToDelete = product
        showDeleteDialog = true
    }

    fun showAppealDialog(product: ProductModel) {
        productToAppeal = product
        showAppealDialog = true
    }

    fun showHandoverDialog(product: ProductModel) {
        productToHandover = product
        showHandoverDialog = true
    }

    fun showVerifyReturnDialog(product: ProductModel) {
        productToVerifyReturn = product
        showVerifyReturnDialog = true
    }
}

@Composable
fun RenderDialogs(
    dialogState: DialogState,
    context: android.content.Context,
    productViewModel: ProductViewModel,
    uId: String?,
    refreshData: () -> Unit
) {
    with(dialogState) {
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

        if (showCodPaymentDialog && productToMarkPaid != null) {
            AlertDialog(
                onDismissRequest = {
                    showCodPaymentDialog = false
                    productToMarkPaid = null
                },
                title = { Text("Confirm Cash Payment", color = Color.White) },
                text = {
                    Column {
                        Text("Product: ${productToMarkPaid?.title}", color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Amount: NPR. ${String.format("%.2f", productToMarkPaid?.price?.times(productToMarkPaid?.rentalDays ?: 1) ?: 0.0)}",
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Have you received cash payment from the renter?", color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ This will mark the product as PAID and allow handover.",
                            color = Color.Yellow,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            productToMarkPaid?.let { product ->
                                productViewModel.completeCashPayment(product.productId) { success, message ->
                                    if (success) {
                                        Toast.makeText(context, "Cash payment confirmed", Toast.LENGTH_SHORT).show()
                                        refreshData()
                                    } else {
                                        Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            showCodPaymentDialog = false
                            productToMarkPaid = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) { Text("Yes, Received Payment") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCodPaymentDialog = false
                        productToMarkPaid = null
                    }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }

        if (showHandoverDialog && productToHandover != null) {
            AlertDialog(
                onDismissRequest = { showHandoverDialog = false },
                title = { Text("Handover Product", color = Color.White) },
                text = {
                    Column {
                        Text("Confirm product handover to renter?", color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Rental period starts now", color = Color.LightGray, fontSize = 12.sp)
                        Text("• Renter cannot cancel after handover", color = Color.LightGray, fontSize = 12.sp)
                        Text("• Product will be marked as rented out", color = Color.LightGray, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            productToHandover?.let { product ->
                                productViewModel.handoverProduct(product.productId) { success, msg ->
                                    if (success) {
                                        Toast.makeText(context, "Product handed over", Toast.LENGTH_SHORT).show()
                                        refreshData()
                                    } else {
                                        Toast.makeText(context, "Failed: $msg", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            showHandoverDialog = false
                            productToHandover = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) { Text("Confirm Handover") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showHandoverDialog = false
                        productToHandover = null
                    }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }

        if (showVerifyReturnDialog && productToVerifyReturn != null) {
            AlertDialog(
                onDismissRequest = { showVerifyReturnDialog = false },
                title = { Text("Verify Product Return", color = Color.White) },
                text = {
                    Column {
                        Text("Product: ${productToVerifyReturn?.title}", color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Check the product condition and confirm return:", color = Color.LightGray)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            productToVerifyReturn?.let { product ->
                                productViewModel.verifyReturn(product.productId) { success, msg, _ ->
                                    if (success) {
                                        Toast.makeText(context, "Return verified", Toast.LENGTH_SHORT).show()
                                        refreshData()
                                    } else {
                                        Toast.makeText(context, "Failed: $msg", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            showVerifyReturnDialog = false
                            productToVerifyReturn = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) { Text("Verify Return") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showVerifyReturnDialog = false
                        productToVerifyReturn = null
                    }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }
    }
}

@Composable
fun ListedItemCard(
    product: ProductModel,
    isFlaggedTab: Boolean,
    dialogState: DialogState,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onAppealClicked: () -> Unit,
    onAcceptClicked: () -> Unit,
    onRejectClicked: () -> Unit,
    onHandoverClicked: () -> Unit,
    onVerifyReturnClicked: () -> Unit,
    onChatClicked: () -> Unit
) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    var requesterName by remember { mutableStateOf<String?>(null) }

    val isFlagged = product.flagged
    val hasAppeal = product.appealReason.isNotEmpty()
    val imageUrl = product.imageUrl.firstOrNull() ?: ""

    val status = when {
        isFlagged && hasAppeal -> "APPEAL PENDING"
        isFlagged -> "FLAGGED"
        product.rentalStatus == ProductViewModel.STATUS_PENDING -> "PENDING APPROVAL"
        product.rentalStatus == ProductViewModel.STATUS_APPROVED -> {
            if (product.paymentMethod == "Cash on Delivery") {
                "APPROVED - AWAITING PICKUP"
            } else {
                "APPROVED - AWAITING PAYMENT"
            }
        }
        product.rentalStatus == ProductViewModel.STATUS_PAID -> "PAID - AWAITING HANDOVER"
        product.rentalStatus == ProductViewModel.STATUS_RENTED -> "RENTED OUT"
        product.rentalStatus == ProductViewModel.STATUS_RETURNING -> "RETURN REQUESTED"
        product.outOfStock -> "UNAVAILABLE"
        else -> "AVAILABLE"
    }

    val showChatButton = product.rentalRequesterId.isNotEmpty() &&
            (product.rentalStatus == ProductViewModel.STATUS_PENDING ||
                    product.rentalStatus == ProductViewModel.STATUS_APPROVED ||
                    product.rentalStatus == ProductViewModel.STATUS_PAID ||
                    product.rentalStatus == ProductViewModel.STATUS_RENTED ||
                    product.rentalStatus == ProductViewModel.STATUS_RETURNING)

    LaunchedEffect(product.rentalRequesterId) {
        if (product.rentalRequesterId.isNotEmpty()) {
            userViewModel.getUserById(product.rentalRequesterId) { success, _, user ->
                if (success) requesterName = user?.fullName ?: "Unknown User"
            }
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFlagged -> Color.Red.copy(alpha = 0.1f)
                product.rentalStatus == ProductViewModel.STATUS_RENTED -> Field.copy(alpha = 0.6f)
                product.outOfStock -> Color.Gray.copy(alpha = 0.4f)
                product.rentalStatus == ProductViewModel.STATUS_PENDING ||
                        product.rentalStatus == ProductViewModel.STATUS_APPROVED -> Color.Blue.copy(alpha = 0.2f)
                else -> Field
            }
        ),
        border = when {
            isFlagged && !hasAppeal -> BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
            hasAppeal -> BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.3f))
            else -> null
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = imageUrl,
                    placeholder = painterResource(R.drawable.rentrimage),
                    error = painterResource(R.drawable.rentrimage),
                    contentDescription = product.title,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        product.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Box(
                        modifier = Modifier.background(
                            when {
                                isFlagged && !hasAppeal -> Color.Red.copy(alpha = 0.2f)
                                hasAppeal -> Color.Cyan.copy(alpha = 0.2f)
                                product.rentalStatus == ProductViewModel.STATUS_RENTED -> Color.Red.copy(alpha = 0.2f)
                                product.rentalStatus == ProductViewModel.STATUS_PENDING ||
                                        product.rentalStatus == ProductViewModel.STATUS_APPROVED -> Color.Blue.copy(alpha = 0.2f)
                                else -> Orange.copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(6.dp)
                        ).padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = status,
                            color = when {
                                isFlagged && !hasAppeal -> Color.Red
                                hasAppeal -> Color.Cyan
                                product.rentalStatus == ProductViewModel.STATUS_RENTED -> Color.White
                                product.rentalStatus == ProductViewModel.STATUS_PENDING ||
                                        product.rentalStatus == ProductViewModel.STATUS_APPROVED -> Color.White
                                else -> Orange
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (product.rentalRequesterId.isNotEmpty() && requesterName != null) {
                        Text(
                            "Requested by: $requesterName",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    if (isFlagged && !isFlaggedTab) {
                        Text(
                            "Flagged by ${product.flaggedBy.size} user(s)",
                            color = Color.Red.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }

                    if (hasAppeal) {
                        Text("✓ Appeal submitted", color = Color.Cyan, fontSize = 10.sp)
                    }

                    Text(
                        "NPR. ${String.format("%.2f", product.price)}/day",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

                if (!isFlagged && product.rentalStatus.isEmpty() && !product.outOfStock) {
                    Row {
                        IconButton(
                            onClick = onEditClicked,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, "Edit", tint = Color.LightGray)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDeleteClicked,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                        }
                    }
                }
            }

            if (showChatButton ||
                product.rentalStatus == ProductViewModel.STATUS_PENDING ||
                product.rentalStatus == ProductViewModel.STATUS_APPROVED ||
                product.rentalStatus == ProductViewModel.STATUS_PAID ||
                product.rentalStatus == ProductViewModel.STATUS_RETURNING) {

                Divider(
                    color = Color.Gray.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showChatButton) {
                        TextButton(
                            onClick = onChatClicked,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Orange
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Chat, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Chat with Renter", fontSize = 12.sp)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    when (product.rentalStatus) {
                        ProductViewModel.STATUS_PENDING -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onRejectClicked,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red.copy(alpha = 0.9f),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp)
                                ) {
                                    Text("Reject", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }

                                Button(
                                    onClick = onAcceptClicked,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Green.copy(alpha = 0.9f),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp)
                                ) {
                                    Text("Accept", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        ProductViewModel.STATUS_APPROVED -> {
                            if (product.paymentMethod == "Cash on Delivery") {
                                Button(
                                    onClick = { dialogState.showCodPaymentDialog(product) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Cash Received", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Payment: ${product.paymentMethod}",
                                        color = if (product.paymentMethod == "Cash on Delivery") Color.Yellow else Color.Cyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "Awaiting ${if (product.paymentMethod == "Cash on Delivery") "pickup & payment" else "payment"}",
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        ProductViewModel.STATUS_PAID -> {
                            Button(
                                onClick = onHandoverClicked,
                                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Handshake, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Handover Product", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        ProductViewModel.STATUS_RETURNING -> {
                            Button(
                                onClick = onVerifyReturnClicked,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Verify Return", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}