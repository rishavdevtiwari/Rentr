package com.example.rentr

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange

class ProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Retrieve data from intent
        val productName = intent.getStringExtra("productName") ?: "Sample Product"
        val productImg = intent.getIntExtra("productImg", R.drawable.bicycle) // Default image
        val productPrice = intent.getIntExtra("productPrice", 500)
        setContent {
            // Pass data to the composable
            ProductDisplay(productName, productImg, productPrice)
        }
    }
}

@Composable
fun ProductDisplay(productName: String, productImg: Int, productPrice: Int) {
    val context = LocalContext.current
    val activity = context as? Activity // Safe cast

    //Random list of sellers
    val list_of_sellers = listOf("Pragyan Khati","Girish Basnet","Nischal Gautam","Rishav Dev Tiwari","Sadie Clayton")

    var quantity by remember { mutableStateOf(1) }
    val productQuantity = remember { (1..15).random() }
    val productRating = remember { (1..5).random()}
    val sellerId = remember{(0..4).random()}

    val availability by remember { mutableStateOf(listOf(true, false).random()) }

    val totalPrice = productPrice * quantity

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            if (availability) { // Only show bottom bar if available
                BottomBar(price = totalPrice)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)) {
                Image(
                    painter = painterResource(id = productImg),
                    contentDescription = productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { activity?.finish() }, // Use safe cast
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Field.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(productName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    // Availability Card
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (availability) Orange.copy(alpha = 0.15f) else Field
                        )
                    ) {
                        Text(
                            text = if (availability) "Available" else "Unavailable",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (availability) Orange else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = Orange, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$productRating (4,749 reviews)", color = Color.Gray, fontSize = 14.sp)
                }

                if (availability) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Field)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Listed By", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("${list_of_sellers[sellerId]}", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))       
                    Divider(color = Field)
                    Text("Description", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Items are product to market risk, please do not enjoy your life until you are at the brink of it. Keep safe and live. I am never gonna dance again guilty feeling though its easy to pretend.", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(20.dp))


                    Spacer(modifier = Modifier.height(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Quantity", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(16.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Field)
                        ) {
                            Text(
                                text = "Available for Rent: $productQuantity",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .background(Field, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) {
                            Text("-", color = Color.White, fontSize = 24.sp)
                        }
                        Text(quantity.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { if(quantity<productQuantity) quantity++ }) {
                            Text("+", color = Color.White, fontSize = 24.sp)
                        }
                    }
                } else {
                    // Show centered "Not in stock" card when unavailable
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp), // Add padding for spacing
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Red.copy(alpha = 0.8f) // Red card
                            )
                        ) {
                            Text(
                                text = "Not in stock",
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                                color = Color.White, // White text
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBar(price: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Total price", color = Color.Gray, fontSize = 12.sp)
            Text("NPR. $price", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pay to Rent", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ProductDisplayPreview() {
    ProductDisplay("Venesa Long Shirt", R.drawable.bicycle, 320)
}
