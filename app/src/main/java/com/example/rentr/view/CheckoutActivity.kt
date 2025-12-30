package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.model.TransactionModel
import com.example.rentr.repository.TransactionRepoImpl
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.TransactionViewModel
import com.example.rentr.viewmodel.UserViewModel
import java.util.UUID

class CheckoutActivity : ComponentActivity() {

    private val transactionViewModel: TransactionViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TransactionViewModel(TransactionRepoImpl()) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val productTitle = intent.getStringExtra("productTitle") ?: "Product"
        val basePrice = intent.getDoubleExtra("basePrice", 0.0) // Get base price
        val rentalPrice = intent.getDoubleExtra("rentalPrice", 0.0)
        val days = intent.getIntExtra("days", 1)
        val productId = intent.getStringExtra("productId") ?: ""
        val sellerId = intent.getStringExtra("sellerId") ?: ""

        setContent {
            RentrTheme {
                CheckoutScreen(
                    productTitle = productTitle,
                    basePrice = basePrice, // Pass base price
                    rentalPrice = rentalPrice,
                    days = days,
                    productId = productId,
                    sellerId = sellerId,
                    viewModel = transactionViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    productTitle: String,
    basePrice: Double, // Receive base price
    rentalPrice: Double,
    days: Int,
    productId: String,
    sellerId: String,
    viewModel: TransactionViewModel
) {
    var location by remember { mutableStateOf("") }
    val paymentOptions = listOf("Cash on Delivery", "Pay Online via Khalti")
    var selectedPayment by remember { mutableStateOf(paymentOptions[0]) }
    val context = LocalContext.current
    val activity = context as? Activity

    val userViewModel = remember { UserViewModel(UserRepoImp1()) }
    val currentUser = userViewModel.getCurrentUser()

    val isLoading by viewModel.isLoading.observeAsState(false)
    val transactionResult by viewModel.transactionResult.observeAsState()

    LaunchedEffect(transactionResult) {
        transactionResult?.let {
            val (success, message) = it
            if (success) {
                Toast.makeText(context, "Order for $productTitle confirmed!", Toast.LENGTH_LONG).show()
                activity?.finish()
            } else {
                Toast.makeText(context, "Order failed: ${message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun confirmOrder() {
        if (location.isBlank()) {
            Toast.makeText(context, "Please enter a delivery location", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentUser == null) {
            Toast.makeText(context, "You must be logged in to place an order", Toast.LENGTH_SHORT).show()
            return
        }

        val startTimeMillis = System.currentTimeMillis()
        val rentalDurationMillis = days * 24L * 60 * 60 * 1000 // Use Long for calculation
        val endTimeMillis = startTimeMillis + rentalDurationMillis

        val transaction = TransactionModel(
            transactionId = UUID.randomUUID().toString(),
            productId = productId,
            renterId = currentUser.uid,
            sellerId = sellerId,
            basePrice = basePrice, // Set the base price here
            rentalPrice = rentalPrice,
            days = days,
            paymentOption = selectedPayment,
            pickupLocation = location,
            startTime = startTimeMillis.toString(),
            endTime = endTimeMillis.toString() // Set the calculated end time
        )


        viewModel.addTransaction(transaction)
    }

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
                onClick = { confirmOrder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirm Order", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                }
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
            Text("Total Price for $days day(s)", color = Color.Gray)
            Text(String.format("NPR. %.2f", rentalPrice), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

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
