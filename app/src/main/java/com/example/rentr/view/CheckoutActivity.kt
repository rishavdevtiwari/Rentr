package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.ViewModelProvider
import com.example.rentr.model.TransactionModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.TransactionRepoImpl
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.TransactionViewModel
import com.example.rentr.viewmodel.UserViewModel
// --- KHALTI IMPORTS ---

import com.khalti.checkout.Khalti
import com.khalti.checkout.data.Environment
import com.khalti.checkout.data.KhaltiPayConfig
import java.util.UUID

class CheckoutActivity : ComponentActivity() {

    private val transactionViewModel: TransactionViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TransactionViewModel(TransactionRepoImpl()) as T
            }
        }
    }

    private val productViewModel: ProductViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProductViewModel(ProductRepoImpl()) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val productTitle = intent.getStringExtra("productTitle") ?: "Product"
        val basePrice = intent.getDoubleExtra("basePrice", 0.0)
        val rentalPrice = intent.getDoubleExtra("rentalPrice", 0.0)
        val days = intent.getIntExtra("days", 1)
        val productId = intent.getStringExtra("productId") ?: ""
        val sellerId = intent.getStringExtra("sellerId") ?: ""

        setContent {
            RentrTheme {
                CheckoutScreen(
                    productTitle = productTitle,
                    basePrice = basePrice,
                    rentalPrice = rentalPrice,
                    days = days,
                    productId = productId,
                    sellerId = sellerId,
                    transactionViewModel = transactionViewModel,
                    productViewModel = productViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    productTitle: String,
    basePrice: Double,
    rentalPrice: Double,
    days: Int,
    productId: String,
    sellerId: String,
    transactionViewModel: TransactionViewModel,
    productViewModel: ProductViewModel
) {
    var location by remember { mutableStateOf("") }
    val paymentOptions = listOf("Cash on Delivery", "Pay Online via Khalti")
    var selectedPayment by remember { mutableStateOf(paymentOptions[0]) }

    val context = LocalContext.current
    val activity = context as? Activity
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val currentUser = userViewModel.getCurrentUser()

    // Observe State from ViewModel
    val isLoading by transactionViewModel.isLoading.observeAsState(false)
    val transactionResult by transactionViewModel.transactionResult.observeAsState()
    val pidx by transactionViewModel.pidx.observeAsState() // <--- The backend response

    // --- 1. HANDLE KHALTI OPENING ---
    // This block runs automatically when 'pidx' gets a value from the backend
    LaunchedEffect(pidx) {
        pidx?.let { paymentId ->
            Log.d("Khalti", "PIDX received: $paymentId. Opening Khalti...")

            val config = KhaltiPayConfig(
                publicKey = "bdf7c03c426241909b72382bb1359159", // TODO: Paste your TEST PUBLIC KEY here
                pidx = paymentId,
                environment = Environment.TEST
            )

            val khalti = Khalti.init(
                context,
                config,
                onPaymentResult = { result, khalti ->
                    Log.d("Khalti", "Payment Success: $result")

                    // Payment successful! Now save transaction to Firebase
                    val transaction = TransactionModel(
                        transactionId = UUID.randomUUID().toString(),
                        productId = productId,
                        renterId = currentUser!!.uid,
                        sellerId = sellerId,
                        basePrice = basePrice,
                        rentalPrice = rentalPrice,
                        days = days,
                        paymentOption = "Khalti (Paid)",
                        pickupLocation = location,
                        startTime = System.currentTimeMillis().toString(),
                        endTime = (System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000)).toString()
                    )
                    transactionViewModel.addTransaction(transaction)
                    khalti.close()
                },
                onMessage = { payload, khalti ->
                    Log.e("Khalti", "Message: ${payload.message}")
                    khalti.close()
                }
            )
            khalti.open()
        }
    }

    // --- 2. HANDLE FINAL DATABASE SUCCESS ---
    LaunchedEffect(transactionResult) {
        transactionResult?.let { (success, message) ->
            if (success) {
                // Mark product as out of stock
                productViewModel.getProductById(productId) { productSuccess, _, product ->
                    if (productSuccess && product != null) {
                        val updatedProduct = product.copy(outOfStock = true, rentalStatus = "")
                        productViewModel.updateProduct(productId, updatedProduct) { updateSuccess, _ ->
                            if (updateSuccess) {
                                Toast.makeText(context, "Order Confirmed!", Toast.LENGTH_LONG).show()
                                activity?.finish()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Transaction Failed: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun confirmOrder() {
        if (location.isBlank()) {
            Toast.makeText(context, "Please enter a delivery location", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentUser == null) {
            Toast.makeText(context, "Login required", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPayment == "Pay Online via Khalti") {
            // STEP A: Call Backend to get PIDX
            transactionViewModel.initiateKhaltiPayment(
                rentalPrice = rentalPrice,
                productId = productId,
                productName = productTitle
            )
        } else {
            // Cash on Delivery Flow
            val transaction = TransactionModel(
                transactionId = UUID.randomUUID().toString(),
                productId = productId,
                renterId = currentUser.uid,
                sellerId = sellerId,
                basePrice = basePrice,
                rentalPrice = rentalPrice,
                days = days,
                paymentOption = "Cash on Delivery",
                pickupLocation = location,
                startTime = System.currentTimeMillis().toString(),
                endTime = (System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000)).toString()
            )
            transactionViewModel.addTransaction(transaction)
        }
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            Text("Item", color = Color.Gray)
            Text(productTitle, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Total Price for $days day(s)", color = Color.Gray)
            Text(String.format("NPR. %.2f", rentalPrice), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color.Gray.copy(alpha = 0.5f))

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

            Text("Payment Method", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                paymentOptions.forEach { option ->
                    Row(
                        Modifier.fillMaxWidth()
                            .selectable(selected = (option == selectedPayment), onClick = { selectedPayment = option })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedPayment),
                            onClick = { selectedPayment = option },
                            colors = RadioButtonDefaults.colors(selectedColor = Orange, unselectedColor = Color.Gray)
                        )
                        Text(text = option, modifier = Modifier.padding(start = 8.dp), color = Color.White)
                    }
                }
            }
        }
    }
}