package com.example.rentr.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel

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
    val tabs = listOf("Available / Unavailable", "Rented Out", "Flagged")

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

    val filteredList = when (selectedTabIndex) {
        0 -> products.filter { !it.outOfStock && !(it.flagged && it.flaggedBy.isNotEmpty()) }
        1 -> products.filter { it.outOfStock && !(it.flagged && it.flaggedBy.isNotEmpty()) }
        2 -> products.filter { it.flagged && it.flaggedBy.isNotEmpty() }
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
                ) {
                    Text("Yes, Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
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
                    Text("Reason for flagging: ${productToAppeal?.flaggedReason}", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = appealReason,
                        onValueChange = { appealReason = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
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
                                    Toast.makeText(context, "Appeal submitted", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        showAppealDialog = false
                        appealReason = ""
                        productToAppeal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) {
                    Text("Submit Appeal")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showAppealDialog = false
                    appealReason = ""
                    productToAppeal = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = { ListedTopAppBar() },
        floatingActionButton = {
            if (selectedTabIndex != 2) {
                FloatingActionButton(
                    onClick = {
                        if (!isUserVerified) {
                            Toast.makeText(
                                context,
                                "This feature is locked for you. You are not verified yet.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val intent = Intent(context, NewListingActivity::class.java)
                            (context as Activity).startActivityForResult(intent, 1)
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
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
                        text = {
                            Text(
                                text = title,
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
                        isFlaggedTab = selectedTabIndex == 2,
                        onEditClicked = {
                            val isProductFlagged = product.flagged && product.flaggedBy.isNotEmpty()
                            val isRented = product.outOfStock

                            if (!isProductFlagged && !isRented) {
                                val intent = Intent(context, EditListedActivity::class.java)
                                intent.putExtra("productId", product.productId)
                                (context as Activity).startActivityForResult(intent, 2)
                            } else {
                                val message = if (isProductFlagged)
                                    "Cannot edit flagged products. Submit an appeal first."
                                else
                                    "Cannot edit rented out products."
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDeleteClicked = {
                            val isProductFlagged = product.flagged && product.flaggedBy.isNotEmpty()
                            val isRented = product.outOfStock

                            if (!isProductFlagged && !isRented) {
                                productToDelete = product
                                showDeleteDialog = true
                            } else {
                                val message = if (isProductFlagged)
                                    "Cannot delete flagged products. Submit an appeal first."
                                else
                                    "Cannot delete rented out products."
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onAppealClicked = {
                            val isProductFlagged = product.flagged && product.flaggedBy.isNotEmpty()
                            if (isProductFlagged && product.appealReason.isEmpty()) {
                                productToAppeal = product
                                showAppealDialog = true
                            } else if (product.appealReason.isNotEmpty()) {
                                Toast.makeText(context, "Appeal already submitted", Toast.LENGTH_SHORT).show()
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
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onAppealClicked: () -> Unit
) {
    val status = when {
        product.flagged && product.flaggedBy.isNotEmpty() -> "FLAGGED"
        product.outOfStock -> "Rented Out"
        !product.availability -> "Unavailable"
        else -> "Available"
    }

    val isRented = status == "Rented Out"
    val isUnavailable = status == "Unavailable"
    val isFlagged = status == "FLAGGED"
    val imageUrl = product.imageUrl.firstOrNull()
    val hasAppeal = product.appealReason.isNotEmpty()

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .height(if (isFlaggedTab) 120.dp else 100.dp)
            .width(320.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFlagged -> Color.Yellow.copy(alpha = 0.1f)
                isRented -> Field.copy(alpha = 0.6f)
                isUnavailable -> Color.Gray.copy(alpha = 0.4f)
                else -> Field
            }
        ),
        border = if (isFlagged) BorderStroke(1.dp, Color.Yellow.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl,
                placeholder = painterResource(R.drawable.rentrimage),
                error = painterResource(R.drawable.p1),
                contentDescription = product.title,
                modifier = Modifier
                    .size(if (isFlaggedTab) 80.dp else 70.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = product.title,
                    color = if (isUnavailable) Color.LightGray else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .background(
                            when {
                                isFlagged -> Color.Yellow.copy(alpha = 0.2f)
                                isRented -> Color.Red.copy(alpha = 0.6f)
                                isUnavailable -> Color.Gray.copy(alpha = 0.4f)
                                else -> Orange.copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = status,
                        color = when {
                            isFlagged -> Color.Yellow
                            isRented -> Color.White
                            isUnavailable -> Color.LightGray
                            else -> Orange
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "NPR. ${product.price}/day",
                    color = if (isUnavailable) Color.LightGray else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (isFlagged && product.flaggedReason.isNotEmpty()) {
                    Text(
                        text = "Flagged: ${product.flaggedReason.joinToString(", ").take(30)}...",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }

                if (isFlagged && hasAppeal) {
                    Box(
                        modifier = Modifier
                            .background(Color.Cyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "APPEAL SUBMITTED",
                            color = Color.Cyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isFlagged) {
                IconButton(
                    onClick = onAppealClicked,
                    enabled = !hasAppeal
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Appeal",
                        tint = if (hasAppeal) Color.Gray else Color.Cyan
                    )
                }
            } else {
                Row {
                    IconButton(
                        onClick = onEditClicked,
                        enabled = !isRented
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Item",
                            tint = if (isRented) Color.Gray else Orange
                        )
                    }
                    IconButton(
                        onClick = onDeleteClicked,
                        enabled = !isRented
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item",
                            tint = if (isRented) Color.Gray else Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListedTopAppBar() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "My Listed Items",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}