package com.example.rentr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.rentr.model.ChatConversation
import com.example.rentr.model.ChatMessage
import com.example.rentr.repository.ChatRepo
import com.example.rentr.repository.ChatRepoImpl

class ChatViewModel(private val chatRepo: ChatRepo = ChatRepoImpl()) : ViewModel() {

    private val _conversations = MutableLiveData<List<ChatConversation>>()
    val conversations: LiveData<List<ChatConversation>> = _conversations

    fun startOrGetConversation(
        productId: String,
        renterId: String,
        sellerId: String,
        initialMessage: String?=null,
        callback: (conversationId: String?) -> Unit
    ) {
        chatRepo.startOrGetConversation(
            productId, 
            renterId, 
            sellerId, 
            initialMessage, 
            callback
        )
    }

    fun sendMessage(conversationId: String, message: ChatMessage, callback: (success: Boolean) -> Unit) {
        chatRepo.sendMessage(conversationId, message, callback)
    }

    fun getMessages(conversationId: String): LiveData<List<ChatMessage>> {
        return chatRepo.getMessages(conversationId).asLiveData()
    }

    fun getConversations(userId: String) {
        chatRepo.getConversations(userId) { conversationList ->
            _conversations.postValue(conversationList)
        }
    }
}
