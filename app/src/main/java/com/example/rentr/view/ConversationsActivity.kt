package com.example.rentr.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.example.rentr.model.ChatConversation
import com.example.rentr.model.ProductModel
import com.example.rentr.model.UserModel
import com.example.rentr.repository.ProductRepoImpl
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.RentrTheme
import com.example.rentr.viewmodel.ChatViewModel
import com.example.rentr.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

class ConversationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentrTheme {
                ConversationsScreen(onBackClicked = { onBackPressedDispatcher.onBackPressed() })
            }
        }
    }
}

// A small, dedicated ViewModel for each item in the conversation list
class ConversationItemViewModel(
    private val productRepo: ProductRepoImpl = ProductRepoImpl(),
    private val userRepo: UserRepoImpl = UserRepoImpl()
) : ViewModel() {

    private val _product = MutableLiveData<ProductModel?>()
    val product: LiveData<ProductModel?> = _product

    private val _otherUser = MutableLiveData<UserModel?>()
    val otherUser: LiveData<UserModel?> = _otherUser

    fun loadConversationDetails(productId: String, otherUserId: String) {
        productRepo.getProductById(productId) { _, _, product ->
            _product.postValue(product)
        }
        userRepo.getUserById(otherUserId) { _, _, user ->
            _otherUser.postValue(user)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(onBackClicked: () -> Unit) {
    val chatViewModel = remember { ChatViewModel() }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val conversations by chatViewModel.conversations.observeAsState(emptyList())
    val currentUserId = userViewModel.getCurrentUser()?.uid ?: ""

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            chatViewModel.getConversations(currentUserId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Chats", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(conversations) { conversation ->
                ConversationItem(conversation = conversation, currentUserId = currentUserId)
            }
        }
    }
}

@Composable
fun ConversationItem(conversation: ChatConversation, currentUserId: String) {
    val context = LocalContext.current
    val itemViewModel = remember { ConversationItemViewModel() }

    // Determine the other user's ID for display purposes
    val otherUserId = if (currentUserId == conversation.sellerId) conversation.renterId else conversation.sellerId

    // Observe the dynamically loaded data
    val product by itemViewModel.product.observeAsState()
    val otherUser by itemViewModel.otherUser.observeAsState()

    // Load the data when the composable is first displayed
    LaunchedEffect(Unit) {
        itemViewModel.loadConversationDetails(conversation.productId, otherUserId)
    }

    val titleText = if (currentUserId == conversation.renterId) {
        product?.title ?: "Loading..."
    } else {
        otherUser?.fullName ?: "Loading..."
    }

    val imageUrl = product?.imageUrl?.firstOrNull() ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("CONVERSATION_ID", conversation.conversationId)
                    putExtra("CHAT_TITLE", titleText)
                }
                context.startActivity(intent)
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Product Image",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titleText, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                conversation.lastMessage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = formatTimestamp(conversation.lastMessageTimestamp),
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
    HorizontalDivider(color = Color(0xFF2C2C2E))
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
