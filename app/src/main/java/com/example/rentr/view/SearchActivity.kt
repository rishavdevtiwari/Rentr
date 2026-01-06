package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.viewmodel.ProductViewModel

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SearchScreen(onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onBack: () -> Unit) {
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val allProducts by productViewModel.allProducts.observeAsState(emptyList())

    var query by remember { mutableStateOf("") }
    var minPrice by remember { mutableStateOf(0.0) }
    var maxPrice by remember { mutableStateOf(Double.MAX_VALUE) }
    var showFilterOptions by remember { mutableStateOf(false) }

    // Sorting State: 0 = Default, 1 = Ascending, 2 = Descending
    var priceSortState by remember { mutableStateOf(0) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        productViewModel.getAllProducts { _, _, _ -> }
        focusRequester.requestFocus()
    }

    // 1. Filter the results first
    val baseResults = allProducts.filter {
        val matchesQuery = it.title.contains(query, ignoreCase = true)
        val matchesPrice = it.price in minPrice..maxPrice
        it.verified && !it.flagged && matchesQuery && matchesPrice
    }

    // 2. Apply sorting logic
    val filteredResults = when (priceSortState) {
        1 -> baseResults.sortedBy { it.price }
        2 -> baseResults.sortedByDescending { it.price }
        else -> baseResults
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Search", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterOptions = !showFilterOptions }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Toggle Filter Bar",
                            tint = if (showFilterOptions) Orange else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Search Field
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Search for items...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Orange) },
                shape = RoundedCornerShape(18.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Field,
                    focusedTextColor = Color.Black
                )
            )

            // Subtle Price Sort Bar (Appears on clicking Filter Icon)
            if (showFilterOptions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            // Cycle through 0 -> 1 -> 2 -> 0
                            priceSortState = (priceSortState + 1) % 3
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (priceSortState > 0) Orange.copy(alpha = 0.2f) else Field,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (priceSortState > 0) Orange else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Price",
                                color = if (priceSortState > 0) Orange else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = when(priceSortState) {
                                    1 -> Icons.Default.KeyboardArrowUp
                                    2 -> Icons.Default.KeyboardArrowDown
                                    else -> Icons.Default.UnfoldMore
                                },
                                contentDescription = null,
                                tint = if (priceSortState > 0) Orange else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Button to open the Min/Max Price Modal
                    TextButton(onClick = { /* show price modal from previous prompt */ }) {
                        Text("Range", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            // Results List
            if (filteredResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredResults) { product ->
                        SearchProductItem(product)
                    }
                }
            }
        }
    }
}
@Composable
fun SearchProductItem(product: ProductModel) {
    Column {
        ProductCard(product)
        Spacer(modifier = Modifier.height(8.dp))
        Text(product.title, color = Orange, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text("NPR. ${product.price}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun PriceFilterChips(min: Double, max: Double, onClear: () -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        SuggestionChip(
            onClick = onClear,
            label = {
                val label = if (max == Double.MAX_VALUE) "Min: NPR $min" else "NPR $min - $max"
                Text(label, color = Orange)
            },
            icon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = Orange) }
        )
    }
}

@Composable
fun PriceFilterModal(currentMin: Double, currentMax: Double, onDismiss: () -> Unit, onApply: (Double, Double) -> Unit) {
    var minText by remember { mutableStateOf(if (currentMin == 0.0) "" else currentMin.toString()) }
    var maxText by remember { mutableStateOf(if (currentMax == 0.0) "" else currentMax.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        title = { Text("Filter by Price", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minText,
                    onValueChange = { minText = it },
                    label = { Text("Min Price") },
                    colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White, focusedBorderColor = Orange)
                )
                OutlinedTextField(
                    value = maxText,
                    onValueChange = { maxText = it },
                    label = { Text("Max Price") },
                    colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White, focusedBorderColor = Orange)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onApply(minText.toDoubleOrNull() ?: 0.0, maxText.toDoubleOrNull() ?: 0.0) }, colors = ButtonDefaults.buttonColors(containerColor = Orange)) {
                Text("Apply")
            }
        }
    )
}