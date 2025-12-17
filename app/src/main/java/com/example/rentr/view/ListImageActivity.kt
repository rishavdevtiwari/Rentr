package com.example.rentr.view

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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.R
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import kotlin.collections.plus

class ListImageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RentrTheme {
                ImageEditorScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen() {
    var images by remember { mutableStateOf(List(4) { R.drawable.car }) }
    val context = LocalContext.current
    val minImages = 4
    val maxImages = 7

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Images", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Handle back navigation */ }) {
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
                        Toast.makeText(context, "Please add at least $minImages photos.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Changes Saved!", Toast.LENGTH_SHORT).show()
                        // TODO: Add actual save logic here
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
            itemsIndexed(images) { index, imageRes ->
                Box(modifier = Modifier.size(110.dp)) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Selected product image",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { images = images.toMutableList().also { it.removeAt(index) } },
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
                            .size(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .clickable {
                                images = images + R.drawable.car // Placeholder
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = Color.Gray, modifier = Modifier.size(40.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ImageEditorScreenPreview() {
    RentrTheme {
        ImageEditorScreen()
    }
}
