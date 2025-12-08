package com.example.rentr

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    var currentStep by remember { mutableStateOf(1) }
    val steps = listOf("Citizenship Front", "Citizenship Back", "Pan Card", "Bank A/c Details", "Profile Pic")

    Scaffold(
        topBar = { KYCTopBar { if (currentStep > 1) currentStep-- } },
        containerColor = primaryColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            KYCStepper(steps = steps, currentStep = currentStep)
            Spacer(modifier = Modifier.height(30.dp))

            // Dynamic content based on the current step
            when (currentStep) {
                1 -> DocumentStepContent(title = "Citizenship Front")
                2 -> DocumentStepContent(title = "Citizenship Back")
                3 -> DocumentStepContent(title = "Pan Card")
                4 -> DocumentStepContent(title = "Bank A/c Details")
                5 -> DocumentStepContent(title = "Profile Pic")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { if (currentStep < steps.size) currentStep++ else {
                    Toast.makeText(context, "Verification Successful", Toast.LENGTH_SHORT).show()
                    activity?.finish()
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
    }
}

@Composable
fun DocumentStepContent(title: String) {
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
        DocumentUploadBox(title = "Tap to upload", modifier = Modifier.fillMaxWidth())
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
                StepCircle(stepNumber = index + 1, isActive = index + 1 == currentStep)
                if (index < steps.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp,
                        color = textLightColor.copy(alpha = 0.3f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
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

@Composable
fun DocumentUploadBox(title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
        onClick = {}
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = textLightColor, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = textLightColor, fontSize = 14.sp)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
fun KYCScreenPreview() {
    KYCScreen()
}
