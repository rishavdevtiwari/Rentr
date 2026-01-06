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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.ui.theme.Orange
import com.example.rentr.viewmodel.ProductViewModel

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialQuery = intent.getStringExtra("SEARCH_QUERY") ?: ""

        setContent {
            SearchScreen(initialQuery, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(initialQuery: String, onBack: () -> Unit) {
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val allProducts by productViewModel.allProducts.observeAsState(emptyList())
    var query by remember { mutableStateOf(initialQuery) }

    // Fetch products once on launch
    LaunchedEffect(Unit) {
        productViewModel.getAllProducts { _, _, _ -> }
    }

    val filteredResults = allProducts.filter {
        it.verified && !it.flagged && it.title.contains(query, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Results", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Search field within SearchActivity for further refinement
            SearchBar(query = query, onQueryChange = { query = it })

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredResults.isEmpty()) {
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
    // Reusing the same logic structure as Dashboard's ProductGrid but optimized for Grid
    Column {
        ProductCard(product) // copied code for intent
        Spacer(modifier = Modifier.height(8.dp))
        Text(product.title, color = Orange, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text("NPR. ${product.price}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}