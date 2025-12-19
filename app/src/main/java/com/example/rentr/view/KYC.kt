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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.rentr.repository.UserRepoImp1
import com.example.rentr.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// region Palette
private val primaryColor = Color(0xFF1E1E1E)
private val accentColor = Color(0xFFFF6200)
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
// endregion

class KYC : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KYCScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KYCScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    val userViewModel = remember { UserViewModel(UserRepoImp1()) }
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(1) }
    val steps = listOf("Citizenship Front", "Citizenship Back", "Pan Card", "Bank A/c Details", "Profile Pic")

    var selectedImageUris by remember { mutableStateOf<Map<Int, Uri>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUris = selectedImageUris + (currentStep to it)
        }
    }

    LaunchedEffect(Unit) {
        userViewModel.getCurrentUser()?.uid?.let {
            userViewModel.getUserById(it) { _, _, _ -> }
        }
    }

    Scaffold(
        topBar = {
            KYCTopBar {
                if (currentStep > 1) {
                    currentStep--
                } else {
                    activity?.finish()
                }
            }
        },
        containerColor = primaryColor
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
            ) {
                KYCStepper(steps = steps, currentStep = currentStep)
                Spacer(modifier = Modifier.height(30.dp))

                // Dynamic content based on the current step
                val currentTitle = steps[currentStep - 1]
                DocumentStepContent(
                    title = currentTitle,
                    selectedUri = selectedImageUris[currentStep],
                    onUploadClick = { imagePickerLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (currentStep < steps.size) {
                            if (selectedImageUris[currentStep] == null) {
                                Toast.makeText(context, "Please upload an image for this step.", Toast.LENGTH_SHORT).show()
                            } else {
                                currentStep++
                            }
                        } else {
                            // Final submission logic
                            coroutineScope.launch {
                                isLoading = true
                                val user = userViewModel.user.value
                                val userId = userViewModel.getCurrentUser()?.uid

                                if (userId == null || user == null) {
                                    Toast.makeText(context, "Could not find user.", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                    return@launch
                                }

                                if (selectedImageUris.size < steps.size) {
                                    Toast.makeText(context, "Please upload all required documents.", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                    return@launch
                                }

                                val uploadedUrls = mutableListOf<String>()
                                for (i in 1..steps.size) {
                                    val uri = selectedImageUris[i]
                                    if (uri != null) {
                                        val url = uploadKycImage(uri)
                                        if (url != null) {
                                            uploadedUrls.add(url)
                                        } else {
                                            Toast.makeText(context, "Upload failed for ${steps[i-1]}.", Toast.LENGTH_SHORT).show()
                                            isLoading = false
                                            return@launch
                                        }
                                    }
                                }

                                val updatedUser = user.copy(kycUrl = uploadedUrls)
                                userViewModel.updateProfile(userId, updatedUser) { success, msg ->
                                    if (success) {
                                        Toast.makeText(context, "KYC Submitted Successfully!", Toast.LENGTH_LONG).show()
                                        activity?.setResult(Activity.RESULT_OK)
                                        activity?.finish()
                                    } else {
                                        Toast.makeText(context, "Failed to submit KYC: $msg", Toast.LENGTH_SHORT).show()
                                    }
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(
                        text = if (currentStep < steps.size) "Next Step" else "Submit for Verification",
                        color = textColor, fontWeight = FontWeight.Medium
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(primaryColor.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentColor)
                }
            }
        }
    }
}

@Composable
fun DocumentStepContent(title: String, selectedUri: Uri?, onUploadClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Upload $title",
            color = textColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please upload a clear image of your document.",
            color = textLightColor,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        DocumentUploadBox(
            selectedUri = selectedUri,
            onClick = onUploadClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DocumentUploadBox(selectedUri: Uri?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (selectedUri != null) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Selected Document",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = textLightColor, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tap to upload", color = textLightColor, fontSize = 14.sp)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KYCTopBar(onBackClicked: () -> Unit) {
    TopAppBar(
        title = { Text("KYC") },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = primaryColor,
            titleContentColor = textColor,
            navigationIconContentColor = textColor
        )
    )
}

@Composable
private fun KYCStepper(steps: List<String>, currentStep: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, _ ->
                StepCircle(stepNumber = index + 1, isActive = index + 1 <= currentStep)
                if (index < steps.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp,
                        color = if (index + 1 < currentStep) accentColor else textLightColor.copy(alpha = 0.3f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            steps.forEachIndexed { index, title ->
                StepLabel(label = title, isActive = index + 1 == currentStep)
            }
        }
    }
}

@Composable
private fun StepCircle(stepNumber: Int, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isActive) accentColor else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isActive) accentColor else textLightColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$stepNumber",
            color = if (isActive) textColor else textLightColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StepLabel(label: String, isActive: Boolean) {
    Text(
        text = label,
        color = if (isActive) accentColor else textLightColor,
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
        lineHeight = 12.sp,
        modifier = Modifier.width(60.dp) // Set a fixed width to ensure labels wrap and align nicely
    )
}

private suspend fun uploadKycImage(uri: Uri): String? {
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
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    continuation.resume(null)
                }
            })
            .dispatch()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
fun KYCScreenPreview() {
    KYCScreen()
}