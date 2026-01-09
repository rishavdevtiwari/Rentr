package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
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

class RentalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentalScreen()
        }
    }
}

@Composable
fun RentalScreen() {
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val userId = userViewModel.getCurrentUser()?.uid

    val products by productViewModel.allProducts.observeAsState(emptyList())

    LaunchedEffect(userId) {
        userId?.let {
            productViewModel.getAllProducts { _, _, _ -> }
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "Ongoing Rents", "Past Rentals")

    val pendingRentals = products.filter { it.rentalRequesterId == userId && it.rentalStatus == "pending" }
    val ongoingRentals = products.filter { it.rentalRequesterId == userId && it.rentalStatus == "approved" && !it.outOfStock }
    val pastRentals = products.filter { it.rentalRequesterId == userId && it.outOfStock }

    Scaffold(
        containerColor = Color.Black,
        topBar = { RentalTopAppBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Black,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
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
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal, color = if (selectedTabIndex == index) Color.White else Color.Gray) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> RentalsList(rentals = pendingRentals, isPending = true)
                1 -> RentalsList(rentals = ongoingRentals, isOngoing = true)
                2 -> RentalsList(rentals = pastRentals)
            }
        }
    }
}

@Composable
fun RentalTopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("My Rentals", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun RentalsList(rentals: List<ProductModel>, isPending: Boolean = false, isOngoing: Boolean = false) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(rentals) { rental ->
            RentalCard(rental = rental, isPending = isPending, isOngoing = isOngoing)
        }
    }
}

@Composable
fun RentalCard(rental: ProductModel, isPending: Boolean, isOngoing: Boolean) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val scope = rememberCoroutineScope()
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(150.dp),
        colors = CardDefaults.cardColors(containerColor = Field)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = rental.imageUrl.firstOrNull(),
                contentDescription = rental.title,
                modifier = Modifier.size(110.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.rentrimage),
                error = painterResource(id = R.drawable.rentrimage)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(rental.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                when {
                    isPending -> {
                        Text("Rental Request Pending", color = Color.Gray, fontSize = 14.sp)
                    }
                    isOngoing -> {
                        Button(onClick = {
                            val intent = Intent(context, CheckoutActivity::class.java).apply {
                                putExtra("productId", rental.productId)
                                putExtra("productTitle", rental.title)
                                putExtra("basePrice", rental.price)
                                putExtra("rentalPrice", rental.price * rental.rentalDays)
                                putExtra("days", rental.rentalDays)
                                putExtra("productId", rental.productId)
                                putExtra("sellerId", rental.listedBy)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Pay NPR. ${rental.price * rental.rentalDays}", color = Color.Black)
                        }
                    }
                // Item is paid and currently in possession (outOfStock is true)
                    isOngoing && rental.outOfStock && rental.rentalStatus == "approved" -> {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        productViewModel.updateRentalStatus(rental.productId, "returning") { success, _ ->
                            if(success) Toast.makeText(context, "Marked as returned. Waiting for owner.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("End Rental / Return")
                }
            }
                    rental.rentalStatus == "returning" -> {
                        Text("Return Pending Confirmation", color = Orange)
                    }
            }
                Text("NPR. ${rental.price}/day", color = Orange, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
