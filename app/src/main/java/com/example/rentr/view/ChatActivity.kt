package com.example.rentr.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.model.ChatMessage
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.Orange
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ChatViewModel
import com.example.rentr.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val conversationId = intent.getStringExtra("CONVERSATION_ID")
        val chatTitle = intent.getStringExtra("CHAT_TITLE") ?: "Chat"

        if (conversationId == null) {
            finish()
            return
        }

        setContent {
            RentrTheme {
                ChatScreen(
                    conversationId = conversationId,
                    chatTitle = chatTitle,
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(conversationId: String, chatTitle: String, onBackClicked: () -> Unit) {
    val chatViewModel = remember { ChatViewModel() }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val messages by chatViewModel.getMessages(conversationId).observeAsState(emptyList())
    val currentUserId = userViewModel.getCurrentUser()?.uid ?: ""
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chatTitle, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            MessageInput(value = text, onValueChange = { text = it }) {
                if (text.isNotBlank()) {
                    val message = ChatMessage(
                        conversationId = conversationId,
                        senderId = currentUserId,
                        text = text
                    )
                    chatViewModel.sendMessage(conversationId, message) { success ->
                        if (success) {
                            text = ""
                            coroutineScope.launch {
                                if(messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message, currentUserId)
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, currentUserId: String) {
    val isFromCurrentUser = message.senderId == currentUserId
    val alignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isFromCurrentUser) Orange else Color(0xFF2C2C2E)
    val textColor = if (isFromCurrentUser) Color.Black else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isFromCurrentUser) 64.dp else 0.dp,
                end = if (isFromCurrentUser) 0.dp else 64.dp
            ),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun MessageInput(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color(0xFF1A1A1A),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Orange),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.Black)
        }
    }
}
