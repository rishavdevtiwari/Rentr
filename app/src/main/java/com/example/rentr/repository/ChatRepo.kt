package com.example.rentr.repository

import com.example.rentr.model.ChatConversation
import com.example.rentr.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepo {

    fun startOrGetConversation(
        productId: String,
        renterId: String,
        sellerId: String,
        initialMessage: String,
        callback: (conversationId: String?) -> Unit
    )

    fun sendMessage(
        conversationId: String,
        message: ChatMessage,
        callback: (success: Boolean) -> Unit
    )


    fun getMessages(conversationId: String): Flow<List<ChatMessage>>


    fun getConversations(userId: String, callback: (List<ChatConversation>) -> Unit)
}
