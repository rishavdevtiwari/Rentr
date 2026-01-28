package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
    var showFilterOptions by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }

    // Sorting States: 0 = Default, 1 = High-to-Low, 2 = Low-to-High
    var priceSortState by remember { mutableStateOf(0) }
    var ratingSortState by remember { mutableStateOf(0) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        productViewModel.getAllProducts { _, _, _ -> }
        focusRequester.requestFocus()
    }

    // 1. Base Filter (Query + Verification + Category)
    var results = allProducts.filter {
        val matchesQuery = it.title.contains(query, ignoreCase = true)
        val matchesCategory = if (selectedCategory == "All") true else it.category == selectedCategory
        it.verified && !it.flagged && matchesQuery && matchesCategory
    }

    // 2. Apply Price Sorting
    results = when (priceSortState) {
        1 -> results.sortedByDescending { it.price }
        2 -> results.sortedBy { it.price }
        else -> results
    }

    // 3. Apply Rating Sorting If active
    results = when (ratingSortState) {
        1 -> results.sortedByDescending { it.rating }
        2 -> results.sortedBy { it.rating }
        else -> results
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
                            contentDescription = "Toggle Filters",
                            tint = if (showFilterOptions) Orange else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Searchng Field
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("What are you looking for?", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Orange) },
                shape = RoundedCornerShape(18.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Field,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    cursorColor = Orange,
                    focusedIndicatorColor = Orange
                )
            )

            // Filter Bar (Price & Rating & Categories)
            if (showFilterOptions) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // PRIXE SORT BUTTON
                        FilterToggleChip(
                            label = "Price",
                            state = priceSortState,
                            onToggle = {
                                priceSortState = (priceSortState + 1) % 3
                                if (priceSortState > 0) ratingSortState = 0 // Reset other sort
                            }
                        )

                        // RATING SOET BUTTON
                        FilterToggleChip(
                            label = "Rating",
                            state = ratingSortState,
                            onToggle = {
                                ratingSortState = (ratingSortState + 1) % 3
                                if (ratingSortState > 0) priceSortState = 0 // Reset other sort
                            }
                        )
                    }

                    // lazy row for selection of categories
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            CategorySearchChip("All", selectedCategory == "All") { selectedCategory = "All" }
                        }
                        // same categories as dashbord
                        items(categories) { category ->
                            CategorySearchChip(
                                category.name,
                                selectedCategory == category.name
                            ) { selectedCategory = category.name }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Product Grid for search screen.
            if (results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products found", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(results) { product ->
                        SearchProductItem(product)
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySearchChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Orange else Field,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(0.3f))
    ) {
        Text(
            text = name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun FilterToggleChip(label: String, state: Int, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(12.dp),
        color = if (state > 0) Orange.copy(alpha = 0.15f) else Field,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (state > 0) Orange else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (state > 0) Orange else Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = when (state) {
                    1 -> Icons.Default.KeyboardArrowDown // High to Low
                    2 -> Icons.Default.KeyboardArrowUp   // Low to High
                    else -> Icons.Default.UnfoldMore
                },
                contentDescription = null,
                tint = if (state > 0) Orange else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
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