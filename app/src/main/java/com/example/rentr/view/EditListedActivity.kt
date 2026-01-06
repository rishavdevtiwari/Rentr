package com.example.rentr.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rentr.R
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.ui.theme.Field
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel

class EditListedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val productId = intent.getStringExtra("productId")
        if (productId == null) {
            finish()
            return
        }
        setContent {
            RentrTheme {
                EditScreen(productId = productId, onBackClicked = { onBackPressedDispatcher.onBackPressed() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(productId: String, onBackClicked: () -> Unit) {

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = context as? Activity

    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val productToEdit by productViewModel.product.observeAsState(null)

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }

    val imageEditorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // The images were changed. Set our own result and finish.
            activity?.setResult(Activity.RESULT_OK)
            activity?.finish()
        }
    }

    // Fetch the product when the screen loads
    LaunchedEffect(productId) {
        productViewModel.getProductById(productId) { _, _, _ ->
            // The observer will handle the update
        }
    }

    // Populate the fields once the product is fetched
    LaunchedEffect(productToEdit) {
        productToEdit?.let {
            name = it.title
            description = it.description
            isAvailable = it.availability
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Item", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    productToEdit?.let {
                        val updatedProduct = it.copy(
                            title = name,
                            description = description,
                            availability = isAvailable
                        )
                        productViewModel.updateProduct(productId, updatedProduct) { success, msg ->
                            if (success) {
                                Toast.makeText(context, "Item updated successfully!", Toast.LENGTH_SHORT).show()
                                activity?.setResult(Activity.RESULT_OK)
                                activity?.finish()
                            } else {
                                Toast.makeText(context, "Update failed: $msg", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    ) { innerPadding ->
        if (productToEdit == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = productToEdit?.imageUrl?.firstOrNull(),
                        placeholder = painterResource(id = R.drawable.rentrimage),
                        error = painterResource(id = R.drawable.rentrimage),
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            val intent = Intent(context, ListImageActivity::class.java)
                            intent.putExtra("productId", productId)
                            imageEditorLauncher.launch(intent)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Image",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Name
                Text("Item Name", fontWeight = FontWeight.Medium, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Field,
                        focusedIndicatorColor = Orange,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        focusedTrailingIconColor = Color.Black,
                        unfocusedTrailingIconColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Description
                Text("Description", fontWeight = FontWeight.Medium, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Field,
                        focusedIndicatorColor = Orange,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        focusedTrailingIconColor = Color.Black,
                        unfocusedTrailingIconColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Availability
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
                Spacer(modifier = Modifier.height(20.dp))

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(20.dp)) // Extra space before bottom bar
            }
        }
    }
}
