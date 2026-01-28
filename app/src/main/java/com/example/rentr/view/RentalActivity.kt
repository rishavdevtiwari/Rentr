@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Int
import kotlin.collections.List

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
        it.rentalRequesterId == userId && it.rentalStatus in listOf(
            ProductViewModel.STATUS_APPROVED,
            ProductViewModel.STATUS_PAID,
            ProductViewModel.STATUS_RENTED,
            ProductViewModel.STATUS_RETURNING
        )
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
                            0 -> PendingRentalCard(item as ProductModel, productViewModel, userId ?: "")
                            1 -> ActiveRentalCard(item as ProductModel, productViewModel, userId ?: "")
                            else -> {
                                val transaction = item as TransactionModel
                                val transactionProduct = remember(transaction.productId) {
                                    products.find { it.productId == transaction.productId }
                                }
                                if (transactionProduct != null) {
                                    TransactionProductCard(
                                        product = transactionProduct,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                TransactionHistoryCard(transaction)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PendingRentalCard(
    rental: ProductModel,
    viewModel: ProductViewModel,
    userId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        modifier = modifier.fillMaxWidth().clickable {
            val intent = Intent(context, ProductActivity::class.java).apply {
                putExtra("productId", rental.productId)
            }
            context.startActivity(intent)
        }
    ) {
        Column {
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
                        color = Color.Yellow.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "PENDING APPROVAL",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Yellow
                        )
                    }
                }
            }

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
                Column {
                    Text(
                        "Status: Waiting for owner approval",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    Text(
                        "Requested for ${rental.rentalDays} days",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
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
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Text("Cancel Request", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun ActiveRentalCard(
    rental: ProductModel,
    viewModel: ProductViewModel,
    userId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        modifier = modifier.fillMaxWidth().clickable {
            val intent = Intent(context, ProductActivity::class.java).apply {
                putExtra("productId", rental.productId)
            }
            context.startActivity(intent)
        }
    ) {
        Column {
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

                    val (statusText, statusColor) = when(rental.rentalStatus) {
                        ProductViewModel.STATUS_APPROVED -> {
                            if (rental.paymentMethod == "Cash on Delivery") {
                                Pair("APPROVED - PAYMENT PENDING", Color.Green)
                            } else {
                                Pair("APPROVED - PAYMENT PENDING", Color.Green)
                            }
                        }
                        ProductViewModel.STATUS_PAID -> Pair("PAID - AWAITING HANDOVER", Color.Cyan)
                        ProductViewModel.STATUS_RENTED -> Pair("RENTED", Orange)
                        ProductViewModel.STATUS_RETURNING -> Pair("RETURN REQUESTED", Color.Cyan)
                        else -> Pair("ACTIVE", Color.White)
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

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
                Column {
                    if (rental.rentalStartDate > 0) {
                        val startDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(Date(rental.rentalStartDate))
                        val endDate = if (rental.rentalEndDate > 0) {
                            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                .format(Date(rental.rentalEndDate))
                        } else "Not set"

                        Text(
                            "Period: $startDate - $endDate",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        "Duration: ${rental.rentalDays} days",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }

                when {
                    rental.rentalStatus == ProductViewModel.STATUS_APPROVED -> {
                        if (rental.paymentMethod == "Cash on Delivery") {
                            Surface(
                                color = Color.Yellow.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.Yellow.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.Money, "Cash", tint = Color.Yellow, modifier = Modifier.size(18.dp))
                                    Text("Awaiting Pickup & Payment", color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        } else {
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
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CreditCard, null, modifier = Modifier.size(18.dp), tint = Color.Black)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Pay Now",
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    rental.rentalStatus == ProductViewModel.STATUS_PAID -> {
                        Surface(
                            color = Color.Cyan.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.HourglassEmpty, "Waiting", tint = Color.Cyan, modifier = Modifier.size(18.dp))
                                Text("Awaiting Handover", color = Color.Cyan, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    rental.rentalStatus == ProductViewModel.STATUS_RENTED -> {
                        Button(
                            onClick = {
                                viewModel.requestReturn(rental.productId, userId) { success, message ->
                                    if (success) {
                                        Toast.makeText(context, "Return request sent to owner", Toast.LENGTH_SHORT).show()
                                        viewModel.getAllProducts { _, _, _ -> }
                                    } else {
                                        Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.KeyboardReturn, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Request Return", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    rental.rentalStatus == ProductViewModel.STATUS_RETURNING -> {
                        Surface(
                            color = Color.Cyan.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Info, "Awaiting", tint = Color.Cyan, modifier = Modifier.size(18.dp))
                                Text("Awaiting Verification", color = Color.Cyan, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TransactionProductCard(
    product: ProductModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = modifier.fillMaxWidth().clickable {
            val intent = Intent(context, ProductActivity::class.java).apply {
                putExtra("productId", product.productId)
            }
            context.startActivity(intent)
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl.firstOrNull(),
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.rentrimage)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    product.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "NPR. ${product.price}/day",
                    color = Orange,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun TransactionHistoryCard(transaction: TransactionModel) {
    val context = LocalContext.current

    val formattedStartTime = try {
        val timestamp = transaction.startTime.toLongOrNull()
        if (timestamp != null) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(timestamp))
        } else {
            if (transaction.startTime.length >= 10) {
                transaction.startTime.substring(0, 10)
            } else {
                transaction.startTime
            }
        }
    } catch (e: Exception) {
        transaction.startTime
    }

    val formattedEndTime = try {
        val timestamp = transaction.endTime.toLongOrNull()
        if (timestamp != null) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(timestamp))
        } else {
            if (transaction.endTime.length >= 10) {
                transaction.endTime.substring(0, 10)
            } else {
                transaction.endTime
            }
        }
    } catch (e: Exception) {
        transaction.endTime
    }

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
                        fontSize = 18.sp,
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

            Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RentalDetailRow(label = "Duration:", value = "${transaction.days} days")
                RentalDetailRow(label = "Payment Method:", value = transaction.paymentOption)
                RentalDetailRow(label = "Pickup Location:", value = transaction.pickupLocation)
                RentalDetailRow(label = "Payment ID:", value = transaction.paymentId.takeLast(8))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Rental Period:", color = Color.Gray, fontSize = 12.sp)
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("• Start: $formattedStartTime", color = Color.White, fontSize = 12.sp)
                        Text("• End: $formattedEndTime", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val intent = Intent(context, ProductActivity::class.java).apply {
                        putExtra("productId", transaction.productId)
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Orange
                ),
                border = BorderStroke(1.dp, Orange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("View Product Details")
            }
        }
    }
}

@Composable
fun RentalDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}