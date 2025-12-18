package com.example.rentr.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.tooling.preview.Preview
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
    val tabs = listOf("Available / Unavailable", "Rented Out")

    val userViewModel = remember { UserViewModel(UserRepoImp1()) }
    val uId = userViewModel.getCurrentUser()?.uid

    // Launcher for adding a new item
    val newListingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            uId?.let { productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> } }
        }
    }

    // Launcher for editing an item
    val editListingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            uId?.let { productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> } }
        }
    }

    LaunchedEffect(uId) {
        uId?.let {
            productViewModel.getAllProductsByUser(userId = it) { _, _, _ -> }
        }
    }

    val filteredList = if (selectedTabIndex == 0) {
        products.filter { it.availability || !it.availability } // show available + unavailable
    } else {
        products.filter { it.outOfStock } // rented out
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = { ListedTopAppBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, NewListingActivity::class.java)
                    newListingLauncher.launch(intent)
                },
                containerColor = Orange,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, null, tint = Color.Black)
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
                    ListedItemCardCompact(product = product, isAvailableTab = selectedTabIndex == 0, onEditClicked = {
                        val intent = Intent(context, EditListedActivity::class.java)
                        intent.putExtra("productId", product.productId)
                        editListingLauncher.launch(intent)
                    })
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

@Composable
fun ListedItemCardCompact(product: ProductModel, isAvailableTab: Boolean, onEditClicked: () -> Unit) {
    val status = when {
        product.outOfStock -> "Rented Out"
        !product.availability -> "Unavailable"
        else -> "Available"
    }
    val isRented = status == "Rented Out"
    val isUnavailable = status == "Unavailable" && isAvailableTab
    val imageUrl = product.imageUrl.firstOrNull()

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .height(100.dp)
            .width(320.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isRented -> Field.copy(alpha = 0.6f)
                isUnavailable -> Color.Gray.copy(alpha = 0.4f)
                else -> Field
            }
        )
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
                    .size(70.dp)
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

                Text(
                    text = "Qty: ${product.quantity}",
                    color = if (isUnavailable) Color.LightGray else Color.White,
                    fontSize = 11.sp
                )

                Box(
                    modifier = Modifier
                        .background(
                            when {
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
            }

            IconButton(
                onClick = onEditClicked,
                enabled = !isRented
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = if (isRented ) Color.Gray else Orange
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ListedScreenPreview() {
}
