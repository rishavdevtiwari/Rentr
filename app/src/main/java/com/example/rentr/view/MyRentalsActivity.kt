package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
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
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel

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
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val userId = userViewModel.getCurrentUser()?.uid

    val products by productViewModel.allProducts.observeAsState(emptyList())
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "Active", "History")

    LaunchedEffect(userId) {
        productViewModel.getAllProducts { _, _, _ -> }
    }

    // Filter Logic for Renter Flow
    val pendingRentals = products.filter { it.rentalRequesterId == userId && it.rentalStatus == "pending" }
    val activeRentals = products.filter {
        it.rentalRequesterId == userId &&
                (it.rentalStatus == "approved" || it.rentalStatus == "rented" || it.rentalStatus == "returning")
    }
    val pastRentals = products.filter { it.rentalRequesterId == userId && it.rentalStatus == "" && !it.outOfStock && it.availability }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Rentals", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Black,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 2.dp, color = Orange
                    )
                },
                divider = { Divider(color = Color.DarkGray) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontSize = 14.sp, color = if (selectedTabIndex == index) Orange else Color.Gray) }
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
                    else -> pastRentals
                }

                if (currentList.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No items found", color = Color.Gray)
                        }
                    }
                }

                items(currentList) { rental ->
                    RentalCardItem(rental, productViewModel)
                }
            }
        }
    }
}

@Composable
fun RentalCardItem(rental: ProductModel, viewModel: ProductViewModel) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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

                // Dynamic Status Badge
                Surface(
                    color = when(rental.rentalStatus) {
                        "pending" -> Color.Yellow.copy(0.1f)
                        "approved" -> Color.Green.copy(0.1f)
                        "rented" -> Orange.copy(0.1f)
                        "returning" -> Color.Cyan.copy(0.1f)
                        else -> Color.Gray.copy(0.1f)
                    },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = rental.rentalStatus.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(rental.rentalStatus) {
                            "pending" -> Color.Yellow
                            "approved" -> Color.Green
                            "rented" -> Orange
                            "returning" -> Color.Cyan
                            else -> Color.Gray
                        }
                    )
                }
            }

            // Action Section
            Column(horizontalAlignment = Alignment.End) {
                when {
                    rental.rentalStatus == "pending" -> {
                        Icon(Icons.Default.HourglassEmpty, "Pending", tint = Color.Gray)
                    }
                    rental.rentalStatus == "approved" -> {
                        Button(
                            onClick = {
                                val intent = Intent(context, CheckoutActivity::class.java).apply {
                                    putExtra("productId", rental.productId)
                                    putExtra("productTitle", rental.title)
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
                            Text("Pay", color = Color.Black, fontSize = 12.sp)
                        }
                    }
                    rental.rentalStatus == "rented" -> {
                        Button(
                            onClick = {
                                viewModel.updateProduct(rental.productId, rental.copy(rentalStatus = "returning")) { s, _ ->
                                    if(s) Toast.makeText(context, "Return initiated", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Return", fontSize = 12.sp)
                        }
                    }
                    rental.rentalStatus == "returning" -> {
                        Icon(Icons.Default.Info, "Awaiting Owner", tint = Color.Cyan)
                    }
                }
            }
        }
    }
}