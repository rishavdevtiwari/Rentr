@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.model.ProductModel
import com.example.rentr.model.TransactionModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.TransactionRepoImpl
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.TransactionViewModel
import com.example.rentr.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RentalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RentrTheme {
                RentalScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalScreen() {
    val context = LocalContext.current
    val productViewModel = remember {
        ViewModelProvider(
            context as ComponentActivity,
            ProductViewModel.Factory(ProductRepoImpl())
        ).get(ProductViewModel::class.java)
    }
    val transactionViewModel = remember { TransactionViewModel(TransactionRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val userId = userViewModel.getCurrentUser()?.uid

    val products by productViewModel.allProducts.observeAsState(emptyList())
    val transactions by transactionViewModel.renterTransactions.observeAsState(emptyList())
    val isLoading by transactionViewModel.isLoading.observeAsState(false)

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "Active", "History")

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun refreshData() {
        coroutineScope.launch {
            isRefreshing = true
            productViewModel.getAllProducts { _, _, _ -> }
            userId?.let { transactionViewModel.fetchRenterTransactions(it) }
            delay(1000)
            isRefreshing = false
        }
    }

    LaunchedEffect(userId) {
        userId?.let {
            productViewModel.getAllProducts { _, _, _ -> }
            transactionViewModel.fetchRenterTransactions(it)
        }
    }

    val pendingRentals = products.filter {
        it.rentalRequesterId == userId && it.rentalStatus == ProductViewModel.STATUS_PENDING
    }
    val activeRentals = products.filter {
        it.rentalRequesterId == userId &&
                (it.rentalStatus == ProductViewModel.STATUS_APPROVED ||
                        it.rentalStatus == ProductViewModel.STATUS_RENTED ||
                        it.rentalStatus == ProductViewModel.STATUS_RETURNING)
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Rentals", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
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
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
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
                                    title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) Color.White else Color.Gray
                                )
                            }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val currentList = when (selectedTabIndex) {
                        0 -> pendingRentals
                        1 -> activeRentals
                        else -> transactions
                    }

                    if (currentList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = when (selectedTabIndex) {
                                            0 -> Icons.Default.HourglassEmpty
                                            1 -> Icons.Default.ShoppingBag
                                            else -> Icons.Default.History
                                        },
                                        contentDescription = "Empty",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = when (selectedTabIndex) {
                                            0 -> "No pending rentals"
                                            1 -> "No active rentals"
                                            else -> "No rental history"
                                        },
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    items(currentList) { item ->
                        when (selectedTabIndex) {
                            0, 1 -> RentalCardItem(item as ProductModel, productViewModel, userId ?: "")
                            else -> TransactionHistoryCard(item as TransactionModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionHistoryCard(transaction: TransactionModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TXN: ${transaction.transactionId.takeLast(8)}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "NPR. ${String.format("%.2f", transaction.rentalPrice)}",
                        color = Orange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = Color.Green.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "COMPLETED",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.Green,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DetailRow(label = "Duration:", value = "${transaction.days} days")
                DetailRow(label = "Payment:", value = transaction.paymentOption)
                DetailRow(label = "Pickup:", value = transaction.pickupLocation)

                val periodText = if (transaction.startTime.isNotEmpty() && transaction.endTime.isNotEmpty()) {
                    "${transaction.startTime.substring(0, 10)} to ${transaction.endTime.substring(0, 10)}"
                } else {
                    "Date not available"
                }
                DetailRow(label = "Period:", value = periodText)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun RentalCardItem(rental: ProductModel, viewModel: ProductViewModel, userId: String) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = rental.imageUrl.firstOrNull(),
                contentDescription = null,
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.rentrimage)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(rental.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("NPR. ${rental.price}/day", color = Orange, fontSize = 14.sp)

                Surface(
                    color = when(rental.rentalStatus) {
                        ProductViewModel.STATUS_PENDING -> Color.Yellow.copy(0.1f)
                        ProductViewModel.STATUS_APPROVED -> Color.Green.copy(0.1f)
                        ProductViewModel.STATUS_RENTED -> Orange.copy(0.1f)
                        ProductViewModel.STATUS_RETURNING -> Color.Cyan.copy(0.1f)
                        else -> Color.Gray.copy(0.1f)
                    },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = when(rental.rentalStatus) {
                            ProductViewModel.STATUS_PENDING -> "PENDING APPROVAL"
                            ProductViewModel.STATUS_APPROVED -> "APPROVED - PAYMENT PENDING"
                            ProductViewModel.STATUS_RENTED -> "RENTED"
                            ProductViewModel.STATUS_RETURNING -> "RETURN REQUESTED"
                            else -> "AVAILABLE"
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(rental.rentalStatus) {
                            ProductViewModel.STATUS_PENDING -> Color.Yellow
                            ProductViewModel.STATUS_APPROVED -> Color.Green
                            ProductViewModel.STATUS_RENTED -> Orange
                            ProductViewModel.STATUS_RETURNING -> Color.Cyan
                            else -> Color.Gray
                        }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                when {
                    rental.rentalStatus == ProductViewModel.STATUS_PENDING -> {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.HourglassEmpty,
                                    "Pending Approval",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Pending", color = Color.Yellow, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.cancelRentalRequest(rental.productId, userId) { success, msg ->
                                        if (success) {
                                            Toast.makeText(context, "Request cancelled", Toast.LENGTH_SHORT).show()
                                            viewModel.getAllProducts { _, _, _ -> }
                                        } else {
                                            Toast.makeText(context, "Failed: $msg", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red.copy(alpha = 0.9f),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("Cancel Request", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    rental.rentalStatus == ProductViewModel.STATUS_APPROVED -> {
                        Button(
                            onClick = {
                                val intent = Intent(context, CheckoutActivity::class.java).apply {
                                    putExtra("productId", rental.productId)
                                    putExtra("productTitle", rental.title)
                                    putExtra("basePrice", rental.price)
                                    putExtra("rentalPrice", rental.price * rental.rentalDays)
                                    putExtra("days", rental.rentalDays)
                                    putExtra("sellerId", rental.listedBy)
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Orange),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                            Spacer(Modifier.width(4.dp))
                            Text("Pay Now", color = Color.Black, fontSize = 12.sp)
                        }
                    }

                    rental.rentalStatus == ProductViewModel.STATUS_RENTED -> {
                        Button(
                            onClick = {
                                viewModel.requestReturn(rental.productId, userId) { success, message ->
                                    if (success) {
                                        Toast.makeText(context, "Return request sent to owner", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Request Return", fontSize = 12.sp)
                        }
                    }

                    rental.rentalStatus == ProductViewModel.STATUS_RETURNING -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Info, "Awaiting", tint = Color.Cyan, modifier = Modifier.size(16.dp))
                            Text("Awaiting Verification", color = Color.Cyan, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}