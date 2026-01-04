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
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel

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

    val userViewModel = remember { UserViewModel(UserRepoImp1()) }
    val uId = userViewModel.getCurrentUser()?.uid
    var isUserVerified by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductModel?>(null) }

    var showAppealDialog by remember { mutableStateOf(false) }
    var productToAppeal by remember { mutableStateOf<ProductModel?>(null) }
    var appealReason by remember { mutableStateOf("") }

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
        0 -> products.filter { it.rentalStatus != "pending" && !it.outOfStock && !(it.flagged && it.flaggedBy.isNotEmpty()) }
        1 -> products.filter { it.rentalStatus == "pending" && !it.outOfStock }
        2 -> products.filter { it.outOfStock }
        3 -> products.filter { it.flagged && it.flaggedBy.isNotEmpty() }
        else -> emptyList()
    }

    if (showDeleteDialog && productToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete '${productToDelete?.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { product ->
                            productViewModel.deleteProduct(product.productId) { success, _ ->
                                if (success) {
                                    uId?.let { productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> } }
                                }
                            }
                        }
                        showDeleteDialog = false
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Yes, Delete") }
            },
            dismissButton = { Button(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAppealDialog && productToAppeal != null) {
        AlertDialog(
            onDismissRequest = {
                showAppealDialog = false
                appealReason = ""
                productToAppeal = null
            },
            title = { Text("Submit Appeal") },
            text = {
                Column {
                    Text("Appeal for: ${productToAppeal?.title}", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Reason for flagging: ${productToAppeal?.flaggedReason?.joinToString(", ")}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = appealReason,
                        onValueChange = { appealReason = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Enter your appeal reason...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Field,
                            unfocusedContainerColor = Field,
                            focusedIndicatorColor = Orange,
                            unfocusedIndicatorColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = false,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        productToAppeal?.let { product ->
                            val updatedProduct = product.copy(appealReason = appealReason)
                            productViewModel.updateProduct(product.productId, updatedProduct) { success, _ ->
                                if (success) {
                                    uId?.let { productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> } }
                                    Toast.makeText(context, "Appeal submitted for admin review", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to submit appeal", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        showAppealDialog = false
                        appealReason = ""
                        productToAppeal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) { Text("Submit Appeal") }
            },
            dismissButton = {
                Button(onClick = {
                    showAppealDialog = false
                    appealReason = ""
                    productToAppeal = null
                }) { Text("Cancel") }
            }
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
                ) { Icon(Icons.Default.Add, null, tint = Color.Black) }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Black,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 3.dp,
                        color = Orange
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal, color = if (selectedTabIndex == index) Color.White else Color.Gray) }
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
                            val intent = Intent(context, EditListedActivity::class.java)
                            intent.putExtra("productId", product.productId)
                            editListingLauncher.launch(intent)
                        },
                        onDeleteClicked = {
                            productToDelete = product
                            showDeleteDialog = true
                        },
                        onAppealClicked = {
                            productToAppeal = product
                            showAppealDialog = true
                        },
                        onAcceptClicked = {
                            val updatedProduct = product.copy(
                                rentalStatus = "approved",
                                outOfStock = true,
                                rentalStartDate = System.currentTimeMillis()
                            )
                            productViewModel.updateProduct(product.productId, updatedProduct) { success, _ ->
                                if (success) {
                                    uId?.let { productViewModel.getAllProductsByUser(it) { _, _, _ -> } }
                                    Toast.makeText(context, "Rental accepted", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onRejectClicked = {
                            val updatedProduct = product.copy(rentalStatus = "", rentalRequesterId = "")
                            productViewModel.updateProduct(product.productId, updatedProduct) { success, _ ->
                                if (success) {
                                    uId?.let { productViewModel.getAllProductsByUser(it) { _, _, _ -> } }
                                    Toast.makeText(context, "Rental rejected", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
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
    val userViewModel = remember { UserViewModel(UserRepoImp1()) }
    var requesterName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(product.rentalRequesterId) {
        if (isOngoingTab && product.rentalRequesterId.isNotEmpty()) {
            userViewModel.getUserById(product.rentalRequesterId) { success, _, user ->
                if (success && user != null) {
                    requesterName = user.fullName
                }
            }
        }
    }

    val status = when {
        product.rentalStatus == "pending" -> "Pending Request"
        product.flagged && product.flaggedBy.isNotEmpty() -> "FLAGGED"
        product.outOfStock -> "Rented Out"
        !product.availability -> "Unavailable"
        else -> "Available"
    }

    val isRented = status == "Rented Out"
    val isUnavailable = status == "Unavailable"
    val isFlagged = status == "FLAGGED"
    val isPending = status == "Pending Request"
    val imageUrl = product.imageUrl.firstOrNull()
    val hasAppeal = product.appealReason.isNotEmpty()

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.height(if (isFlaggedTab || isOngoingTab) 120.dp else 100.dp).width(320.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFlagged -> Color.Yellow.copy(alpha = 0.1f)
                isRented -> Field.copy(alpha = 0.6f)
                isUnavailable -> Color.Gray.copy(alpha = 0.4f)
                isPending -> Color.Blue.copy(alpha = 0.2f)
                else -> Field
            }
        ),
        border = if (isFlagged) BorderStroke(1.dp, Color.Yellow.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl,
                placeholder = painterResource(R.drawable.rentrimage),
                error = painterResource(R.drawable.p1),
                contentDescription = product.title,
                modifier = Modifier.size(if (isFlaggedTab || isOngoingTab) 80.dp else 70.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(product.title, color = if (isUnavailable) Color.LightGray else Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                Box(
                    modifier = Modifier.background(
                        when {
                            isFlagged -> Color.Yellow.copy(alpha = 0.2f)
                            isRented -> Color.Red.copy(alpha = 0.6f)
                            isUnavailable -> Color.Gray.copy(alpha = 0.4f)
                            isPending -> Color.Blue.copy(alpha = 0.3f)
                            else -> Orange.copy(alpha = 0.2f)
                        },
                        RoundedCornerShape(6.dp)
                    ).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = status,
                        color = when {
                            isFlagged -> Color.Yellow
                            isRented -> Color.White
                            isUnavailable -> Color.LightGray
                            isPending -> Color.White
                            else -> Orange
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isOngoingTab && requesterName != null) {
                    Text("Requested by: $requesterName", color = Color.LightGray, fontSize = 11.sp)
                }

                Text("NPR. ${product.price}/day", color = if (isUnavailable) Color.LightGray else Color.White, fontSize = 12.sp)
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                if (isFlaggedTab) {
                    Button(
                        onClick = onAppealClicked,
                        enabled = !hasAppeal,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasAppeal) Color.Gray else Orange,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.height(35.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, "Appeal", Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (hasAppeal) "Pending" else "Appeal", fontSize = 10.sp)
                        }
                    }
                } else if (isOngoingTab) {
                    Row {
                        Button(onClick = onAcceptClicked, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), modifier = Modifier.height(35.dp)) { Text("Accept", fontSize = 10.sp) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onRejectClicked, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)), modifier = Modifier.height(35.dp)) { Text("Reject", fontSize = 10.sp) }
                    }
                } else {
                    Row {
                        IconButton(onClick = onEditClicked, modifier = Modifier.size(20.dp), enabled = !isRented && !isUnavailable) {
                            Icon(Icons.Default.Edit, "Edit", tint = if (isRented || isUnavailable) Color.DarkGray else Color.LightGray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDeleteClicked, modifier = Modifier.size(20.dp), enabled = !isRented && !isUnavailable) {
                            Icon(Icons.Default.Delete, "Delete", tint = if (isRented || isUnavailable) Color.DarkGray else Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListedTopAppBar() {
    TopAppBar(
        title = { Text("My Listings", color = Color.White, fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
    )
}
