package com.example.rentr.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.viewmodel.ProductViewModel

private val background = Color(0xFF1E1E1E)

class CategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val categoryName = intent.getStringExtra("Category")

        setContent {
            if (categoryName != null) {
                CategoryScreen(categoryName)
            } else {
                finish()
            }
        }
    }
}

// for flagged products.

@Composable
fun CategoryScreen(initialCategory: String) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Tracking the category, initialized from the Dashboard intent
    var currentCategory by remember { mutableStateOf(initialCategory) }

    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val products by productViewModel.allProducts.observeAsState(emptyList())

    // logic to fetch based on whether "All" or a specific category is selected
    LaunchedEffect(currentCategory) {
        if (currentCategory == "All") {
            productViewModel.getAllProducts { _, _, _ -> }
        } else {
            productViewModel.getAllProductsByCategory(currentCategory) { _, _, _ -> }
        }
    }

    // Filter logic for the grid
    val filteredProducts = products.filter {
        val matchesCategory = if (currentCategory == "All") true else it.category == currentCategory
        !it.flagged && it.verified && matchesCategory
    }

    Scaffold(containerColor = Color.Black) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                IconButton(
                    onClick = { activity?.finish() },
                    modifier = Modifier.background(Color(0xFF1E1E1E), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Rentr Categories",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Lazy row including the "All" option
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Manually add the "All" chip first
                item {
                    CategorySearchChip(
                        name = "All",
                        isSelected = currentCategory == "All"
                    ) {
                        currentCategory = "All"
                    }
                }
                // Then add the rest of the categories
                items(categories) { category ->
                    CategorySearchChip(
                        name = category.name,
                        isSelected = currentCategory == category.name
                    ) {
                        currentCategory = category.name
                    }
                }
            }

            // Grid Display
            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items found", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductGridItem(product)
                    }
                }
            }
        }
    }
}
@Composable
fun ProductGridItem(product: ProductModel) {
    Column {
        ProductCard1(product)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = product.title,
            color = Orange,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "NPR. ${product.price}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ProductCard1(product: ProductModel) {
    val context = LocalContext.current
    val imageUrl = product.imageUrl.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ProductActivity::class.java)
                intent.putExtra("productId", product.productId)
                // Pass the image URL to ProductActivity to avoid fetching it again
                //intent.putExtra("productImg", imageUrl) // Optional: If you want to pass the URL
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field)
    ) {
        AsyncImage(
            model = imageUrl,
            placeholder = painterResource(id = R.drawable.rentrimage),
            error = painterResource(id = R.drawable.rentrimage),
            contentDescription = product.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentScale = ContentScale.Crop
        )
    }
}
