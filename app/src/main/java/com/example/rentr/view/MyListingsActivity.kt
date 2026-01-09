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
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel

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
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val products by productViewModel.allProducts.observeAsState(emptyList())
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Available", "Ongoing", "Rented Out", "Flagged")

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val uId = userViewModel.getCurrentUser()?.uid
    var isUserVerified by remember { mutableStateOf(false) }

    // Dialog States
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductModel?>(null) }
    var showAppealDialog by remember { mutableStateOf(false) }
    var productToAppeal by remember { mutableStateOf<ProductModel?>(null) }
    var appealReason by remember { mutableStateOf("") }

    LaunchedEffect(uId) {
        uId?.let { id ->
            productViewModel.getAllProductsByUser(userId = id) { _, _, _ -> }
            userViewModel.getUserById(id) { success, _, user ->
                if (success) isUserVerified = user?.verified ?: false
            }
        }
    }

    val refreshList = { uId?.let { id -> productViewModel.getAllProductsByUser(id) { _, _, _ -> } } }

    val listingLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) refreshList()
    }

    val filteredList = when (selectedTabIndex) {
        0 -> products.filter { it.rentalStatus == "" && !it.outOfStock && !it.flagged }
        1 -> products.filter { (it.rentalStatus in listOf("pending", "approved", "rented", "returning")) && !it.outOfStock }
        2 -> products.filter { it.outOfStock }
        3 -> products.filter { it.flagged }
        else -> emptyList()
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Listings", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = {
                        if (!isUserVerified) Toast.makeText(context, "Account verification required", Toast.LENGTH_LONG).show()
                        else listingLauncher.launch(Intent(context, NewListingActivity::class.java))
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
                indicator = { TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(it[selectedTabIndex]), height = 2.dp, color = Orange) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontSize = 13.sp, color = if (selectedTabIndex == index) Orange else Color.Gray) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { product ->
                    ListedItemCard(
                        product = product,
                        onEdit = { listingLauncher.launch(Intent(context, EditListedActivity::class.java).putExtra("productId", product.productId)) },
                        onDelete = { productToDelete = product; showDeleteDialog = true },
                        onAppeal = { productToAppeal = product; showAppealDialog = true },
                        onAction = { refreshList() }
                    )
                }
            }
        }
    }

    // Deletion Logic
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Listing?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    productToDelete?.let { p ->
                        productViewModel.deleteProduct(p.productId) { success, _ -> if (success) refreshList() }
                    }
                    showDeleteDialog = false
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
            containerColor = Field, titleContentColor = Color.White, textContentColor = Color.LightGray
        )
    }
}

@Composable
fun ListedItemCard(
    product: ProductModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAppeal: () -> Unit,
    onAction: () -> Unit
) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    var requesterName by remember { mutableStateOf("Loading...") }

    LaunchedEffect(product.rentalRequesterId) {
        if (product.rentalRequesterId.isNotEmpty()) {
            userViewModel.getUserById(product.rentalRequesterId) { success, _, user ->
                if (success) requesterName = user?.fullName ?: "Unknown User"
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Field),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imageUrl.firstOrNull(),
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.rentrimage)
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(product.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("NPR. ${product.price}/day", color = Orange, fontSize = 14.sp)

                if (product.rentalStatus.isNotEmpty()) {
                    Text(
                        text = when(product.rentalStatus) {
                            "pending" -> "Requested by: $requesterName"
                            "approved" -> "Awaiting payment from $requesterName"
                            "rented" -> "In possession of $requesterName"
                            "returning" -> "Return initiated by $requesterName"
                            else -> ""
                        },
                        color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Action UI based on Rental Flow State
            Column(horizontalAlignment = Alignment.End) {
                when {
                    product.flagged -> {
                        IconButton(onClick = onAppeal) { Icon(Icons.Default.Gavel, null, tint = Color.Red) }
                    }
                    product.rentalStatus == "pending" -> {
                        Row {
                            IconButton(onClick = {
                                productViewModel.updateProduct(product.productId, product.copy(rentalStatus = "", rentalRequesterId = "")) { s, _ -> if(s) onAction() }
                            }) { Icon(Icons.Default.Close, null, tint = Color.Red) }
                            IconButton(onClick = {
                                productViewModel.updateProduct(product.productId, product.copy(rentalStatus = "approved")) { s, _ -> if(s) onAction() }
                            }) { Icon(Icons.Default.Check, null, tint = Color.Green) }
                        }
                    }
                    product.rentalStatus == "returning" -> {
                        Button(
                            onClick = {
                                productViewModel.updateProduct(product.productId, product.copy(
                                    rentalStatus = "",
                                    rentalRequesterId = "",
                                    outOfStock = false,
                                    availability = true
                                )) { s, _ -> if(s) onAction() }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) { Text("Confirm Return", fontSize = 10.sp) }
                    }
                    product.rentalStatus == "" -> {
                        Row {
                            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        }
                    }
                }
            }
        }
    }
}