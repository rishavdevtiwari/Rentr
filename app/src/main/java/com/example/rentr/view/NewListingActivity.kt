@file:OptIn(ExperimentalMaterial3Api::class)

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
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NewListingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                Scaffold(
                    containerColor = Color.Black,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Create Listing", color = Color.White, fontWeight = FontWeight.Bold) },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black),
                            navigationIcon = {
                                IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
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

@Composable
fun NewListingScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()

    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    // Form States
    var productName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    // UI States
    var isLoading by remember { mutableStateOf(false) }
    var isUserVerified by remember { mutableStateOf(false) }

    val categories = listOf("Vehicles", "Household", "Electronics", "Accessories", "Furniture", "Sports & Adventure", "Baby Items")

    // Check Verification Status on Launch
    LaunchedEffect(Unit) {
        userViewModel.getCurrentUser()?.uid?.let { uid ->
            userViewModel.getUserById(uid) { success, _, user ->
                if (success) isUserVerified = user?.verified ?: false
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (selectedImageUris.size + uris.size <= 7) {
            selectedImageUris = selectedImageUris + uris
        } else {
            Toast.makeText(context, "Maximum 7 photos allowed", Toast.LENGTH_SHORT).show()
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Photo Section
            Text("Photos (Min 4, Max 7)", color = Color.White, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectedImageUris) { uri ->
                    Box(modifier = Modifier.size(110.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUris = selectedImageUris - uri },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(0.6f), CircleShape)
                                .size(24.dp)
                        ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    }
                }
                if (selectedImageUris.size < 7) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.AddAPhoto, null, tint = Orange) }
                    }
                }
            }

            // Input Fields
            ListingField("Product Name", productName, "E.g. Mountain Bike") { productName = it }
            ListingField("Price (NPR / Day)", price, "0.00", KeyboardType.Number) { price = it }

            // Category Dropdown
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Category", color = Color.White, fontWeight = FontWeight.SemiBold)
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select Category", color = Color.Gray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(Field)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = Color.White) },
                                onClick = { selectedCategory = cat; categoryExpanded = false }
                            )
                        }
                    }
                }
            }

            ListingField("Description", description, "Item condition, rules, etc.", height = 120.dp) { description = it }

            // Submit Button
            Button(
                onClick = {
                    if (!isUserVerified) {
                        Toast.makeText(context, "Verify your account to list items", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedImageUris.size < 4 || productName.isBlank() || price.isBlank()) {
                        Toast.makeText(context, "Please complete all fields and add 4 photos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        isLoading = true
                        val uploadedUrls = uploadImagesToCloudinary(selectedImageUris)

                        if (uploadedUrls.size < selectedImageUris.size) {
                            Toast.makeText(context, "Upload failed. Check internet.", Toast.LENGTH_SHORT).show()
                            isLoading = false
                            return@launch
                        }

                        val userId = userViewModel.getCurrentUser()?.uid ?: return@launch
                        val product = ProductModel(
                            title = productName,
                            description = description,
                            availability = isAvailable,
                            category = selectedCategory,
                            listedBy = userId,
                            imageUrl = uploadedUrls,
                            price = price.toDoubleOrNull() ?: 0.0,
                            verified = false // Production: Admin must approve first
                        )

                        productViewModel.addProduct(product) { success, _, productId ->
                            if (success && productId != null) {
                                updateUserSettings(userId, productId, userViewModel) {
                                    isLoading = false
                                    Toast.makeText(context, "Submitted for review!", Toast.LENGTH_SHORT).show()
                                    activity.finish()
                                }
                            } else {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                else Text("List Item", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ListingField(label: String, value: String, hint: String, type: KeyboardType = KeyboardType.Text, height: androidx.compose.ui.unit.Dp = 56.dp, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = height),
            placeholder = { Text(hint, color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = type),
            shape = RoundedCornerShape(12.dp),
            colors = customTextFieldColors()
        )
    }
}

@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Field,
    focusedBorderColor = Orange,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.White,
    cursorColor = Orange
)

private fun updateUserSettings(userId: String, pId: String, vm: UserViewModel, onComplete: () -> Unit) {
    vm.getUserById(userId) { success, _, user ->
        if (success && user != null) {
            val list = (user.listings ?: emptyList()).toMutableList().apply { add(pId) }
            vm.updateProfile(userId, user.copy(listings = list)) { _, _ -> onComplete() }
        } else onComplete()
    }
}

private suspend fun uploadImagesToCloudinary(uris: List<Uri>): List<String> = suspendCoroutine { continuation ->
    val urls = mutableListOf<String>()
    if (uris.isEmpty()) { continuation.resume(emptyList()); return@suspendCoroutine }

    var finished = 0
    uris.forEach { uri ->
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    resultData?.get("secure_url")?.toString()?.let { urls.add(it) }
                    if (++finished == uris.size) continuation.resume(urls)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    if (++finished == uris.size) continuation.resume(urls)
                }
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }
}