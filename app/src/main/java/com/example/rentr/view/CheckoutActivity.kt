package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme

class CheckoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val productTitle = intent.getStringExtra("productTitle") ?: "Product"
        val productPrice = intent.getDoubleExtra("productPrice", 0.0)

        setContent {
            RentrTheme {
                CheckoutScreen(
                    productTitle = productTitle,
                    productPrice = productPrice
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(productTitle: String, productPrice: Double) {
    var location by remember { mutableStateOf("") }
    val paymentOptions = listOf("Cash on Delivery", "Pay Online via Khalti")
    var selectedPayment by remember { mutableStateOf(paymentOptions[0]) }
    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Checkout", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (location.isBlank()) {
                        Toast.makeText(context, "Please enter a delivery location", Toast.LENGTH_SHORT).show()
                    } else {
                        // TODO: Handle order confirmation logic
                        Toast.makeText(context, "Order for $productTitle confirmed!", Toast.LENGTH_LONG).show()
                        activity?.finish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Confirm Order", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Product Summary
            Text("Item", color = Color.Gray)
            Text(productTitle, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Price", color = Color.Gray)
            Text("NPR. $productPrice", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color.Gray.copy(alpha = 0.5f))

            // Location Field
            Text("Delivery Location", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Set Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Orange,
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedLabelColor = Orange,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Method
            Text("Payment Method", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                paymentOptions.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (option == selectedPayment),
                                onClick = { selectedPayment = option }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedPayment),
                            onClick = { selectedPayment = option },
                            colors = RadioButtonDefaults.colors(selectedColor = Orange, unselectedColor = Color.Gray)
                        )
                        Text(
                            text = option,
                            modifier = Modifier.padding(start = 8.dp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}