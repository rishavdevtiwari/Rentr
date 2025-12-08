package com.example.rentr
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange

private val background = Color(0xFF1E1E1E)


class CategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val categoryName = intent.getStringExtra("categoryName")
            if (categoryName != null) {
                CategoryScreen1(categoryName = categoryName)
            } else {
                // Close the activity if no category name is provided
                finish()
            }
        }
    }
}

// Unique data classes for this activity
data class Product1(val id: Int, val name: String, val category: String, val imageRes: Int)
data class Category1(val name: String, val icon: ImageVector)

// Unique categories list for this activity
val categories1 = listOf(
    Category1("Bicycle", Icons.Default.Motorcycle),
    Category1("Bike", Icons.Default.Motorcycle),
    Category1("Camera", Icons.Default.CameraAlt),
    Category1("Car", Icons.Default.DirectionsCar),
    Category1("Toy", Icons.Default.Toys),
    Category1("Furniture", Icons.Default.Chair),
    Category1("Laptop", Icons.Default.Laptop),
    Category1("Kitchen", Icons.Default.Kitchen)
)

@Composable
fun CategoryScreen1(categoryName: String) {
    val context = LocalContext.current
    val activity = context as Activity

    val products = remember(categoryName) {
        (1..10).map { i ->
            val productName = when (categoryName) {
                "Bicycle" -> if (i % 2 == 1) "Mountain Bike" else "City Bicycle"
                "Bike" -> if (i % 2 == 1) "Sports Bike" else "Cruiser Bike"
                "Camera" -> if (i % 2 == 1) "DSLR Camera" else "Mirrorless Camera"
                "Car" -> if (i % 2 == 1) "Modern Sedan" else "Luxury SUV"
                "Toy" -> if (i % 2 == 1) "Action Figure" else "Building Blocks"
                "Furniture" -> if (i % 2 == 1) "Modern Chair" else "Wooden Table"
                "Laptop" -> if (i % 2 == 1) "Gaming Laptop" else "Ultrabook"
                "Kitchen" -> if (i % 2 == 1) "Blender" else "Toaster"
                else -> "Product $i"
            }
            val imageRes = when (categoryName) {
                "Bicycle" -> R.drawable.bicycle
                "Bike" -> R.drawable.bike
                "Camera" -> R.drawable.camera
                "Car" -> R.drawable.car
                "Toy" -> R.drawable.toy
                "Furniture" -> R.drawable.bicycle // Placeholder
                "Laptop" -> R.drawable.camera   // Placeholder
                "Kitchen" -> R.drawable.bike     // Placeholder
                else -> R.drawable.bicycle
            }
            Product1(id = i, name = productName, category = categoryName, imageRes = imageRes)
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { activity.finish() }, // Use safe cast
                        modifier = Modifier
                            .padding(16.dp)
                            .background(background, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = categoryName,
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.padding(horizontal=20.dp))

                }
            }

            items(products) { product ->
                ProductGridItem1(product = product)
            }
        }
    }
}

@Composable
fun ProductGridItem1(product: Product1) {
    Column(horizontalAlignment = Alignment.Start) {
        ProductCard1(product)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = product.name,
            color = Orange,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "NPR. ${(product.id * 157 % 1000) + 500}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ProductCard1(product: Product1) {
    val context = LocalContext.current
    val activity = context as Activity
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ProductActivity::class.java)
                intent.putExtra("productName", product.name)
                intent.putExtra("productImg", product.imageRes)
                intent.putExtra("productPrice", product.id * 157 % 1000 + 500)
                context.startActivity(intent)
                //activity.finish()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Field)
    ) {
        Image(
            painter = painterResource(id = product.imageRes),
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun CategoryScreenPreview1() {
    CategoryScreen1(categoryName = "Car")
}
