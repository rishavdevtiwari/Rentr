package com.example.rentr

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.ui.theme.BG40
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme

// region Palette
private val primaryColor = Color(0xFF1E1E1E)
private val accentColor = Orange
private val textColor = Color.White
private val textLightColor = Color(0xFFAFAFAF)
// endregion

class FaqScreenActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RentrTheme {
                val context = LocalContext.current
                val activity = context as? Activity
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Frequently Asked Questions", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { activity?.finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = primaryColor,
                                titleContentColor = textColor,
                                navigationIconContentColor = textColor
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    FaqScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// 1. Data Model for the questions
data class FaqItem(
    val question: String,
    val answer: String
)

// 2. The Main Screen Composable
@Composable
fun FaqScreen(modifier: Modifier = Modifier) {
    // Sample Data
    val faqList = listOf(
        FaqItem("How do I rent a property?", "Simply browse the listings, select a property you like, and click the 'Rent Now' button to contact the owner."),
        FaqItem("Is my personal data safe?", "Yes, we use industry-standard encryption to ensure your data is always protected."),
        FaqItem("Can I cancel a booking?", "Cancellations depend on the specific property policy. Check the listing details for more information."),
        FaqItem("How do I contact support?", "You can reach us at support@rentr.com or call our hotline available 24/7."),
        FaqItem("What payment methods are accepted?", "We accept Khalti, eSewa, and direct bank transfers.")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = primaryColor)
    ) {
        // Scrollable List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(faqList) { item ->
                FaqCard(item)
            }
        }
    }
}

// 3. The Expandable Card Composable
@Composable
fun FaqCard(faqItem: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BG40),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Question Row with Arrow Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = faqItem.question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = accentColor // Orange arrow
                )
            }

            // Answer (Visible only when expanded)
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = textLightColor.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = faqItem.answer,
                    fontSize = 14.sp,
                    color = textLightColor,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// 4. Preview for Android Studio
@Preview(showBackground = true)
@Composable
fun FaqScreenPreview() {
    RentrTheme {
        FaqScreen()
    }
}
