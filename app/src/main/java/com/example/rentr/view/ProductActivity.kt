package com.example.rentr.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ChatViewModel
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel

class ProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val productId = intent.getStringExtra("productId") ?: ""
        setContent {
            RentrTheme {
                ProductDisplay(productId)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProductDisplay(productId: String) {
    val context = LocalContext.current
    val activity = context as? Activity

    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val currentUserViewModel = remember { UserViewModel(UserRepoImpl()) }
    val sellerViewModel = remember { UserViewModel(UserRepoImpl()) }
    val chatViewModel = remember { ChatViewModel() }

    val product by productViewModel.product.observeAsState()
    val currentUser by currentUserViewModel.user.observeAsState()
    var sellerName by remember { mutableStateOf("") }

    // Dialog States
    var showFlagDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var chatInitialMessage by remember { mutableStateOf("") }
    var rentalDays by remember { mutableStateOf(1) }
    var selectedFlagReason by remember { mutableStateOf("") }
    var customFlagReason by remember { mutableStateOf("") }

    val flagReasons = listOf("Inappropriate Content", "Fake/Scam Product", "Wrong Category", "Price Gouging", "Copyright Infringement", "Other")
    val currentUserId = currentUserViewModel.getCurrentUser()?.uid
    val isSeller = product?.listedBy == currentUserId
    val isUserVerified = currentUser?.verified == true

    // State logic for rental flow
    val isRentedOut = product?.rentalStatus == "rented" || product?.outOfStock == true
    val userAlreadyRequested = product?.rentalRequesterId == currentUserId && product?.rentalStatus == "pending"

    LaunchedEffect(currentUserId) {
        currentUserId?.let { currentUserViewModel.getUserById(it) { _, _, _ -> } }
    }

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) productViewModel.getProductById(productId) { _, _, _ -> }
    }

    LaunchedEffect(product?.listedBy) {
        product?.listedBy?.let { id ->
            sellerViewModel.getUserById(id) { success, _, user ->
                if (success) sellerName = user?.fullName ?: ""
            }
        }
    }

    // Flagging Dialog Logic
    if (showFlagDialog) {
        val isAlreadyFlagged = product?.flaggedBy?.contains(currentUserId) == true
        AlertDialog(
            onDismissRequest = { showFlagDialog = false },
            title = { Text("Flag Item", color = Color.White) },
            text = {
                Column {
                    if (isAlreadyFlagged) Text("Already flagged.", color = Color.Red, fontSize = 12.sp)
                    flagReasons.forEach { reason ->
                        Row(modifier = Modifier.fillMaxWidth().clickable(enabled = !isAlreadyFlagged) {
                            selectedFlagReason = reason
                        }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedFlagReason == reason, onClick = null, enabled = !isAlreadyFlagged)
                            Spacer(modifier = Modifier.width(8.dp)); Text(reason, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    productViewModel.flagProduct(product!!.productId, currentUserId!!, selectedFlagReason) { _, _ -> }
                    showFlagDialog = false
                }, enabled = !isAlreadyFlagged && selectedFlagReason.isNotEmpty()) { Text("Flag") }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    // Chat Dialog Logic
    if (showChatDialog) {
        AlertDialog(
            onDismissRequest = { showChatDialog = false },
            title = { Text("Start Chat", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = chatInitialMessage,
                    onValueChange = { chatInitialMessage = it },
                    label = { Text("Your message") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            },
            confirmButton = {
                Button(onClick = {
                    chatViewModel.startOrGetConversation(product!!.productId, currentUserId!!, product!!.listedBy, chatInitialMessage) { id ->
                        if (id != null) {
                            context.startActivity(Intent(context, ChatActivity::class.java).apply {
                                putExtra("CONVERSATION_ID", id)
                                putExtra("CHAT_TITLE", product!!.title)
                            })
                            showChatDialog = false
                        }
                    }
                }) { Text("Send") }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            if (!isSeller && product != null) {
                ProductBottomBar(
                    product = product!!,
                    rentalDays = rentalDays,
                    isRentedOut = isRentedOut,
                    userAlreadyRequested = userAlreadyRequested,
                    onRentNowClick = {
                        if (!isUserVerified) {
                            Toast.makeText(context, "Verify account to rent.", Toast.LENGTH_LONG).show()
                        } else {
                            val updatedProduct = product!!.copy(rentalStatus = "pending", rentalRequesterId = currentUserId ?: "", rentalDays = rentalDays)
                            productViewModel.updateProduct(product!!.productId, updatedProduct) { success, _ ->
                                if (success) { Toast.makeText(context, "Request sent!", Toast.LENGTH_SHORT).show(); activity?.finish() }
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (product == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Orange) }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                // Image Pager Section
                val pagerState = rememberPagerState { product!!.imageUrl.size }
                Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AsyncImage(model = product!!.imageUrl[page], contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    IconButton(onClick = { activity?.finish() }, modifier = Modifier.padding(16.dp).background(Field.copy(0.5f), CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Section
                    Text(product!!.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Icon(Icons.Default.Star, null, tint = Orange, modifier = Modifier.size(18.dp))
                        Text(" ${product!!.rating} (${product!!.ratingCount} ratings)", color = Color.Gray, fontSize = 14.sp)
                    }

                    if (product!!.rentalStatus == "pending" && !isRentedOut && !userAlreadyRequested) {
                        Text("Others have also requested this item.", color = Color.Yellow, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Description", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(product!!.description, color = Color.Gray)

                    // Rental Duration Selector
                    if (!isSeller) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Rental Days", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (rentalDays > 1) rentalDays-- }, modifier = Modifier.border(1.dp, Color.Gray, CircleShape)) { Icon(Icons.Default.Remove, null, tint = Color.White) }
                                Text("$rentalDays", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                                IconButton(onClick = { rentalDays++ }, modifier = Modifier.border(1.dp, Color.Gray, CircleShape)) { Icon(Icons.Default.Add, null, tint = Color.White) }
                            }
                        }
                    }

                    // Seller Info & Chat
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Seller: $sellerName", color = Color.Gray)
                        if (!isSeller) Button(onClick = { showChatDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Field)) { Text("Chat with Seller", color = Orange) }
                    }

                    // Rating Bar Section
                    if (!isSeller && currentUserId != null) {
                        Spacer(modifier = Modifier.height(24.dp)); Divider(color = Field)
                        Text("Rate this Product", color = Color.White, modifier = Modifier.padding(top = 16.dp))
                        RatingBar(rating = product!!.ratedBy[currentUserId] ?: 0) { newRating ->
                            productViewModel.updateRating(productId, currentUserId, newRating) { _, _ ->
                                productViewModel.getProductById(productId) { _, _, _ -> }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun RatingBar(rating: Int, onRatingChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        (1..5).forEach { index ->
            IconButton(onClick = { onRatingChange(index) }) {
                Icon(Icons.Default.Star, null, tint = if (index <= rating) Orange else Color.Gray, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun ProductBottomBar(product: ProductModel, rentalDays: Int, isRentedOut: Boolean, userAlreadyRequested: Boolean, onRentNowClick: () -> Unit) {
    val totalPrice = product.price * rentalDays
    Row(modifier = Modifier.fillMaxWidth().background(Color.Black).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("Total Price", color = Color.Gray, fontSize = 12.sp)
            Text("NPR. ${String.format("%.2f", totalPrice)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Button(
            onClick = onRentNowClick,
            enabled = !isRentedOut && !userAlreadyRequested,
            colors = ButtonDefaults.buttonColors(containerColor = if (userAlreadyRequested) Color.Gray else Orange),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = when {
                isRentedOut -> "Rented Out"
                userAlreadyRequested -> "Request Sent"
                else -> "Request to Rent"
            }, color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}