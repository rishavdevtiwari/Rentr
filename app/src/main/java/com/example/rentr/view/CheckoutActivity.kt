@file:OptIn(ExperimentalMaterial3Api::class)

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
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.TransactionViewModel
import com.example.rentr.viewmodel.UserViewModel
import com.khalti.checkout.Khalti
import com.khalti.checkout.data.Environment
import com.khalti.checkout.data.KhaltiPayConfig
import java.text.SimpleDateFormat
import java.util.*

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

    val isLoading by transactionViewModel.isLoading.observeAsState(false)
    val transactionResult by transactionViewModel.transactionResult.observeAsState()
    val pidx by transactionViewModel.pidx.observeAsState()

    // Handlers for Transaction Creation
    val createTransactionRecord: (String) -> Unit = { paymentMethod ->
        val currentTime = System.currentTimeMillis()
        val endTime = currentTime + (days * 24L * 60 * 60 * 1000)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val transactionId = "TRX_${currentTime}_${UUID.randomUUID().toString().substring(0, 8)}"

        val transaction = TransactionModel(
            transactionId = transactionId,
            productId = productId,
            renterId = currentUser?.uid ?: "",
            sellerId = sellerId,
            basePrice = basePrice,
            rentalPrice = rentalPrice,
            days = days,
            paymentOption = paymentMethod,
            startTime = dateFormat.format(Date(currentTime)),
            endTime = dateFormat.format(Date(endTime)),
            pickupLocation = location,
            paymentId = "PAY_$currentTime"
        )
        transactionViewModel.addTransaction(transaction)
    }

    // Khalti Integration Logic
    LaunchedEffect(pidx) {
        pidx?.let { paymentId ->
            val config = KhaltiPayConfig(
                publicKey = "bdf7c03c426241909b72382bb1359159",
                pidx = paymentId,
                environment = Environment.TEST
            )

            val khalti = Khalti.init(context, config, onPaymentResult = { result, k ->
                Log.d("Khalti", "Success: $result")
                createTransactionRecord("Khalti (Paid)")
                k.close()
            }, onMessage = { payload, k ->
                Log.e("Khalti", "Error: ${payload.message}")
                k.close()
            })
            khalti.open()
        }
    }

    // Final Success Logic: Syncing Product State - UPDATED
    LaunchedEffect(transactionResult) {
        transactionResult?.let { (success, _) ->
            if (success) {
                if (selectedPayment == "Cash on Delivery") {
                    // For Cash on Delivery, just create transaction record
                    Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_LONG).show()
                    activity?.finish()
                } else {
                    // For online payment, product status will be updated after payment
                    productViewModel.completeCheckout(
                        productId = productId,
                        pickupLocation = location,
                        paymentMethod = selectedPayment,
                        callback = { uSuccess, message ->
                            if (uSuccess) {
                                Toast.makeText(context, "Payment successful! Rental started.", Toast.LENGTH_LONG).show()
                                activity?.finish()
                            } else {
                                Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Complete Rental", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (location.isBlank()) {
                        Toast.makeText(context, "Enter pickup location", Toast.LENGTH_SHORT).show()
                    } else if (selectedPayment == "Pay Online via Khalti") {
                        transactionViewModel.initiateKhaltiPayment(rentalPrice, productId, productTitle)
                    } else {
                        createTransactionRecord("Cash on Delivery")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                else Text(
                    if (selectedPayment == "Cash on Delivery") "Place Order" else "Confirm & Pay",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            // Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Field),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rental Summary", color = Orange, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(productTitle, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text("$days days @ NPR. $basePrice/day", color = Color.Gray, fontSize = 14.sp)
                    Divider(Modifier.padding(vertical = 12.dp), color = Color.DarkGray)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount", color = Color.White)
                        Text("NPR. $rentalPrice", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Pickup / Delivery Info", color = Color.White, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Pickup Location Details") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Orange,
                    cursorColor = Orange
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Payment Selection", color = Color.White, fontWeight = FontWeight.Bold)
            paymentOptions.forEach { option ->
                Row(
                    Modifier.fillMaxWidth()
                        .selectable(selected = (option == selectedPayment), onClick = { selectedPayment = option })
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (option == selectedPayment),
                        onClick = { selectedPayment = option },
                        colors = RadioButtonDefaults.colors(selectedColor = Orange)
                    )
                    Text(option, color = Color.White, modifier = Modifier.padding(start = 12.dp))
                }
            }
        }
    }
}