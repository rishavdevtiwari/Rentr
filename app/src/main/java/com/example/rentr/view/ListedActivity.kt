package com.example.rentr

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange


// Data class to represent a listed item
data class ListedItem(
    val id: Int,
    val name: String,
    val price: Double,
    val imageRes: Int,
    val status: String // "Available", "Unavailable", or "Rented Out"
)

// Sample data for the list, including an "Unavailable" item
val sampleListedItems = listOf(
    ListedItem(1, "Mountain Bike", 55.0, R.drawable.bicycle, "Available"),
    ListedItem(2, "DSLR Camera", 80.0, R.drawable.camera, "Rented Out"),
    ListedItem(6, "Old Camera", 30.0, R.drawable.camera, "Unavailable"),
    ListedItem(3, "Gaming Laptop", 120.0, R.drawable.camera, "Available"),

)

@Composable
fun ListedScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    // Tabs updated to reflect the desired sections
    val tabs = listOf("Available/Unavailable", "Rented Out")

    // Filter logic updated to show "Available" and "Unavailable" items under the first tab
    val filteredList = if (selectedTabIndex == 0) {
        sampleListedItems.filter { it.status.equals("Available", true) || it.status.equals("Unavailable", true) }
    } else {
        sampleListedItems.filter { it.status.equals(tabs[selectedTabIndex], ignoreCase = true) }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            ListedTopAppBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, NewListingActivity::class.java)
                    activity?.startActivity(intent)
                },
                containerColor = Orange,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new listing",
                    tint = Color.Black
                )
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

            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredList) { item ->
                    ListedItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun ListedTopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "My Listed Items",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun ListedItemCard(item: ListedItem) {
    // Add an overall transparency effect if the item is unavailable
    val isUnavailable = item.status.equals("Unavailable", ignoreCase = true)
    val isRented = item.status.equals("Rented Out", ignoreCase = true)

    val context = LocalContext.current
    val activity = context as? Activity

    val cardAlpha = 1f

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(150.dp),
        colors = CardDefaults.cardColors(containerColor = Field.copy(alpha = cardAlpha))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image on the left
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    alpha = cardAlpha // Apply alpha to image as well
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Details in the middle
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.name,
                        color = Color.White.copy(alpha = cardAlpha),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Quantity: 5",
                        color = Color.White.copy(alpha = cardAlpha),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    // Status Tag Logic
                    val statusColor = if (item.status == "Available") Orange else Color.White
                    val statusBackgroundColor = if (item.status == "Available") Orange.copy(alpha = 0.2f) else Color.Red.copy(alpha=0.6f )

                    Box(
                        modifier = Modifier
                            .background(
                                color = statusBackgroundColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.status,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "NPR. ${item.price}/day",
                        color = Color.White.copy(alpha = cardAlpha),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Edit Icon on the right
                IconButton(onClick = {
                    val intent = Intent(context, EditListedActivity::class.java)
                    context.startActivity(intent)
                   }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Item",
                        tint = if(!isRented) Orange else Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ListedScreenPreview() {
    ListedScreen()
}
