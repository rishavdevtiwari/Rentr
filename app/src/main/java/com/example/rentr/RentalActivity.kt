package com.example.rentr

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange

data class Rental(val id: Int, val name: String, val imageRes: Int, val price: Double, val returnDate: String)
data class PastRental(val id: Int, val name: String, val imageRes: Int, val price: Double, val rentalDate: String)

val ongoingRentals = listOf(
    Rental(1, "Mountain Bike", R.drawable.bicycle, 55.0, "July 28, 2024"),
    Rental(2, "DSLR Camera", R.drawable.camera, 80.0, "August 5, 2024")
)

val pastRentals = listOf(
    PastRental(1, "Gaming Laptop", R.drawable.camera, 120.0, "June 15, 2024"),
    PastRental(2, "Old Camera", R.drawable.camera, 30.0, "May 22, 2024")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Ongoing Rents", "Past Rentals", "Rental Shipping")

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            RentalTopAppBar()
        }
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

            when (selectedTabIndex) {
                0 -> OngoingRentalsList(rentals = ongoingRentals)
                1 -> PastRentalsList(rentals = pastRentals)
                2 -> RentalShippingScreen()
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
        Text(
            text = "My Rentals",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun OngoingRentalsList(rentals: List<Rental>) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(rentals) { rental ->
            OngoingRentalCard(rental = rental)
        }
    }
}

@Composable
fun OngoingRentalCard(rental: Rental) {
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
            Image(
                painter = painterResource(id = rental.imageRes),
                contentDescription = rental.name,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(rental.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Return by: ${rental.returnDate}", color = Color.Gray, fontSize = 14.sp)
                Text("NPR. ${rental.price}/day", color = Orange, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun PastRentalsList(rentals: List<PastRental>) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(rentals) { rental ->
            PastRentalCard(rental = rental)
        }
    }
}

@Composable
fun PastRentalCard(rental: PastRental) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .height(150.dp)
            .clickable { /* TODO: Show receipt details */ },
        colors = CardDefaults.cardColors(containerColor = Field)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = rental.imageRes),
                contentDescription = rental.name,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(rental.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Rented on: ${rental.rentalDate}", color = Color.Gray, fontSize = 14.sp)
                Text("Total: NPR. ${rental.price}", color = Orange, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun RentalShippingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Rental Shipping Information", color = Color.White, fontSize = 20.sp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun RentalScreenPreview() {
    RentalScreen()
}
