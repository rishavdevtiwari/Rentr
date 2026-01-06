package com.example.rentr.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Ensure this import exists
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ChatbotViewModel

class ChatbotActivity : ComponentActivity() {

    // 1. Initialize ViewModel
    private val chatbotViewModel: ChatbotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatbotScreen(chatbotViewModel)
                }
            }
        }
    }
}

@Composable
fun ChatbotScreen(viewModel: ChatbotViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var userMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {

        // 2. Chat History List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Space between messages
        ) {
            items(uiState.messages) { message ->
                // 3. Message Bubble Logic
                // If it's the user, align right. If bot, align left.
                val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
                val bubbleColor = if (message.isUser) MaterialTheme.colorScheme.primary else Color.LightGray
                val textColor = if (message.isUser) Color.White else Color.Black

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = alignment
                ) {
                    Text(
                        text = message.text,
                        color = textColor,
                        modifier = Modifier
                            .background(
                                color = bubbleColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                }
            }
        }

        // 4. Input Area
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask something...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        viewModel.sendMessage(userMessage)
                        userMessage = ""
                    }
                }
            ) {
                Text(text = "Send")
            }
        }
    }
}