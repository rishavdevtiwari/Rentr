package com.example.rentr.view

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ProductViewModel
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ListImageActivity : ComponentActivity() {
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
                ImageEditorScreen(productId = productId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(productId: String) {
    var images by remember { mutableStateOf<List<Any>>(emptyList()) }
    var productToEdit by remember { mutableStateOf<ProductModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }

    val minImages = 1
    val maxImages = 7

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val totalImages = images.size + uris.size
        if (totalImages <= maxImages) {
            images = images + uris
        } else {
            Toast.makeText(context, "You can select a maximum of $maxImages photos.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(productId) {
        productViewModel.getProductById(productId) { success, _, fetchedProduct ->
            if (success && fetchedProduct != null) {
                productToEdit = fetchedProduct
                images = fetchedProduct.imageUrl
            }
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Images", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
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
                    if (images.size < minImages) {
                        Toast.makeText(context, "Please add at least $minImages photo.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    coroutineScope.launch {
                        isSaving = true
                        val finalImageUrls = mutableListOf<String>()
                        images.forEach { image ->
                            when (image) {
                                is String -> finalImageUrls.add(image)
                                is Uri -> {
                                    val uploadedUrl = uploadImage(image)
                                    if (uploadedUrl != null) {
                                        finalImageUrls.add(uploadedUrl)
                                    }
                                }
                            }
                        }

                        if (finalImageUrls.size != images.size) {
                            Toast.makeText(context, "Some images failed to upload.", Toast.LENGTH_SHORT).show()
                            isSaving = false
                            return@launch
                        }

                        productToEdit?.let {
                            val updatedProduct = it.copy(imageUrl = finalImageUrls)
                            productViewModel.updateProduct(productId, updatedProduct) { success, _ ->
                                if (success) {
                                    Toast.makeText(context, "Changes Saved!", Toast.LENGTH_SHORT).show()
                                    activity?.setResult(Activity.RESULT_OK)
                                    activity?.finish()
                                } else {
                                    Toast.makeText(context, "Failed to save changes.", Toast.LENGTH_SHORT).show()
                                    isSaving = false // Ensure saving state is reset on failure
                                }
                            }
                        } ?: run {
                            isSaving = false // Ensure saving state is reset if product is null
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                itemsIndexed(images, key = { _, item -> item.hashCode() }) { index, image ->
                    Box(modifier = Modifier.size(110.dp)) {
                        AsyncImage(
                            model = image,
                            contentDescription = "Product image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Close Button
                        IconButton(
                            onClick = { images = images.toMutableList().also { it.removeAt(index) } },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                        }

                        // Reordering Buttons
                        Row(modifier = Modifier.align(Alignment.BottomCenter)) {
                            // Move Left
                            if (index > 0) {
                                IconButton(
                                    onClick = {
                                        images = images.toMutableList().also { Collections.swap(it, index, index - 1) }
                                    },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowLeft, "Move Left", tint = Orange)
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            // Move Right
                            if (index < images.size - 1) {
                                IconButton(
                                    onClick = {
                                        images = images.toMutableList().also { Collections.swap(it, index, index + 1) }
                                    },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowRight, "Move Right", tint = Orange)
                                }
                            }
                        }
                    }
                }

                if (images.size < maxImages) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, "Add Photo", tint = Color.Gray, modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
        }

        if (isSaving) {
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

private suspend fun uploadImage(uri: Uri): String? {
    return suspendCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    continuation.resume(resultData?.get("secure_url")?.toString())
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resume(null)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ImageEditorScreenPreview() {
    RentrTheme {
        ImageEditorScreen(productId = "preview")
    }
}