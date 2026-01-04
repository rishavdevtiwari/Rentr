package com.example.rentr.view

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NewListingActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                Scaffold(
                    containerColor = Color.Black,
                    topBar = {
                        TopAppBar(
                            title = { Text("Create New Listing", color = Color.White) },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                            navigationIcon = {
                                IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    NewListingScreen(modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewListingScreen(modifier: Modifier = Modifier) {
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImp1()) }

    var productName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var isUserVerified by remember { mutableStateOf(false) }

    val minImages = 4
    val maxImages = 7

    LaunchedEffect(Unit) {
        val userId = userViewModel.getCurrentUser()?.uid
        if (userId != null) {
            userViewModel.getUserById(userId) { success, msg, user ->
                if (success) {
                    user?.let { fetchedUser ->
                        isUserVerified = fetchedUser.verified
                        if (!isUserVerified) {
                            Toast.makeText(context, "This feature is locked for you. You are not verified yet.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val totalImages = selectedImageUris.size + uris.size
        if (totalImages <= maxImages) {
            selectedImageUris = selectedImageUris + uris
        } else {
            Toast.makeText(context, "You can select a maximum of $maxImages photos.", Toast.LENGTH_SHORT).show()
        }
    }

    val categories = listOf("Vehicles", "Household", "Electronics", "Accessories", "Furniture", "Sports & Adventure", "Baby Items")
    var categoryExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Add Photos", style = MaterialTheme.typography.titleMedium, color = Color.White)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectedImageUris) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected product image",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUris = selectedImageUris - uri },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove image", tint = Color.White)
                        }
                    }
                }

                if (selectedImageUris.size < maxImages) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = Color.Gray)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Product Name", style = MaterialTheme.typography.titleMedium, color = Color.White)
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Field,
                        focusedBorderColor = Orange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        cursorColor = Orange
                    ),
                    placeholder = { Text("Enter product name", color = Color.Gray) },
                    singleLine = true
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Price (NPR)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Field,
                        focusedBorderColor = Orange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        cursorColor = Orange
                    ),
                    placeholder = { Text("Enter price per day", color = Color.Gray) },
                    singleLine = true
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Description", style = MaterialTheme.typography.titleMedium, color = Color.White)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Field,
                        focusedBorderColor = Orange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        cursorColor = Orange
                    ),
                    placeholder = { Text("Tell us about your product", color = Color.Gray) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Availability", fontWeight = FontWeight.Medium, color = Color.White, fontSize = 16.sp)
                Switch(
                    checked = isAvailable,
                    onCheckedChange = { isAvailable = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Orange,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Field
                    )
                )
            }

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select Category", color = Color.Gray) },
                    trailingIcon = { Icon(if (categoryExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Field,
                        focusedBorderColor = Orange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        cursorColor = Orange
                    )
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.background(Field)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category, color = Color.White) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (!isUserVerified) {
                        Toast.makeText(context, "This feature is locked for you. You are not verified yet.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    if (selectedImageUris.size < minImages) {
                        Toast.makeText(context, "Please add at least $minImages photo(s).", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val priceDouble = price.toDoubleOrNull()
                    if (priceDouble == null || priceDouble <= 0) {
                        Toast.makeText(context, "Please enter a valid price.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val userId = userViewModel.getCurrentUser()?.uid
                    if (userId == null) {
                        Toast.makeText(context, "You must be logged in to list an item.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        isLoading = true
                        val uploadedUrls = uploadImagesToCloudinary(selectedImageUris)

                        if (uploadedUrls.size < selectedImageUris.size) {
                            Toast.makeText(context, "Image upload failed for some images. Please try again.", Toast.LENGTH_LONG).show()
                            isLoading = false
                            return@launch
                        }

                        val model = ProductModel(
                            title = productName,
                            description = description,
                            availability = isAvailable,
                            category = selectedCategory,
                            listedBy = userId,
                            imageUrl = uploadedUrls,
                            price = priceDouble
                        )

                        productViewModel.addProduct(model) { success, msg, productId ->
                            if (success && productId != null) {
                                userViewModel.getUserById(userId) { getUserSuccess, _, user ->
                                    if (getUserSuccess && user != null) {
                                        val updatedListings = user.listings?.toMutableList() ?: mutableListOf()
                                        updatedListings.add(productId)
                                        val updatedUser = user.copy(listings = updatedListings)
                                        userViewModel.updateProfile(userId, updatedUser) { updateUserSuccess, _ ->
                                            isLoading = false
                                            if (updateUserSuccess) {
                                                Toast.makeText(context, "Product listed successfully!", Toast.LENGTH_SHORT).show()
                                                activity.finish()
                                            } else {
                                                Toast.makeText(context, "Failed to update user listings.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(context, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Failed to list product: $msg", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                enabled = !isLoading && isUserVerified
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("List Item", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Orange)
            }
        }
    }
}

private suspend fun uploadImagesToCloudinary(uris: List<Uri>): List<String> = suspendCoroutine { continuation ->
    val uploadedUrls = mutableListOf<String>()
    val uploadCounter = object {
        var count = 0
        fun increment() {
            count++
            if (count == uris.size) {
                continuation.resume(uploadedUrls)
            }
        }
    }

    if (uris.isEmpty()) {
        continuation.resume(emptyList())
        return@suspendCoroutine
    }

    for (uri in uris) {
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    uploadedUrls.add(resultData["url"].toString())
                    uploadCounter.increment()
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    uploadCounter.increment() // Also increment on error to not block forever
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }
}
