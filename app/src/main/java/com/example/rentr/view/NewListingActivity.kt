package com.example.rentr.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.R
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import com.example.rentr.viewmodel.UserViewModel
import kotlin.collections.plus

class NewListingActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

    //Product View Model
    val productViewModel = remember {ProductViewModel(ProductRepoImpl())}

    //User View Model
    val userViewModel = remember { UserViewModel(UserRepoImp1()) }


    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }
    var isAvailable by remember { mutableStateOf(true) }
    var images by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as Activity

    val minImages = 4
    val maxImages = 7

    val categories = listOf("Vehicles", "Household", "Electronics", "Accessories", "Furniture", "Sports & Adventure", "Baby Items")
    var categoryExpanded by remember { mutableStateOf(false) }



    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp) // Increased spacing
    ) {
        // Image selection section
        Text("Add Photos", style = MaterialTheme.typography.titleMedium, color = Color.White)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(images) { imageRes ->
                Box(modifier = Modifier.size(100.dp)) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Selected product image",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { images = images - imageRes },
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

            if (images.size < maxImages) {
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .clickable {
                                if (images.size < maxImages) {
                                    images = images + R.drawable.bicycle // Placeholder
                                } else {
                                    Toast
                                        .makeText(context, "Maximum of $maxImages photos allowed.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = Color.Gray)
                    }
                }
            }
        }

        // Product Name
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

        // Description
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

        // Quantity Stepper
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Quantity", style = MaterialTheme.typography.titleMedium, color = Color.White)
            QuantitySelector(
                quantity = quantity,
                onQuantityChange = { quantity = it }
            )
        }

        // Availability switch
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

        //Category
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded },
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                value = selectedCategory,
                onValueChange = {
                    selectedCategory = it
                },
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

        // Submit button
        // Submit button
        Button(
            onClick = {
                val userId = userViewModel.getCurrentUser()?.uid
                if (userId == null) {
                    Toast.makeText(context, "You must be logged in to list an item.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (images.size < minImages) {
                    Toast.makeText(context, "Please add at least $minImages photos.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val model = ProductModel(
                    title = productName,
                    description = description,
                    quantity = quantity,
                    availability = isAvailable,
                    category = selectedCategory,
                    isVerified = true,
                    listedBy = userId
                )

                // 1. Add the product
                productViewModel.addProduct(model) { success, msg, productId ->
                    if (success && productId != null) {
                        // 2. Update user profile listings
                        userViewModel.getUserById(userId) { getUserSuccess, _, user ->
                            if (getUserSuccess && user != null) {
                                val updatedListings = user.listings?.toMutableList() ?: mutableListOf()
                                updatedListings.add(productId)
                                val updatedUser = user.copy(listings = updatedListings)

                                userViewModel.updateProfile(userId, updatedUser) { updateSuccess, updateMsg ->
                                    if (updateSuccess) {
                                        Toast.makeText(context, "Product listed successfully!", Toast.LENGTH_SHORT).show()
                                        // ✅ Return RESULT_OK so ListedActivity can refresh
                                        (context as Activity).setResult(Activity.RESULT_OK)
                                        (context as Activity).finish()
                                    } else {
                                        Toast.makeText(context, "Error updating profile: $updateMsg", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Could not fetch user to update listings.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to list product: $msg", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange,
                contentColor = Color.Black
            )
        ) {
            Text("List My Item")
        }

    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
            enabled = quantity > 1
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease quantity",
                tint = if (quantity > 1) Orange else Color.Gray
            )
        }

        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )

        IconButton(onClick = { onQuantityChange(quantity + 1) }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase quantity",
                tint = Orange
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun NewListingPreview() {
    RentrTheme {
        NewListingScreen()
    }
}
