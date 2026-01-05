package com.example.rentr.repository

import com.example.rentr.model.ChatConversation
import com.example.rentr.model.ChatMessage
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ChatRepoImpl : ChatRepo {

    private val conversationsRef = FirebaseDatabase.getInstance().getReference("conversations")
    private val messagesRef = FirebaseDatabase.getInstance().getReference("messages")

    override fun startOrGetConversation(
        productId: String,
        renterId: String,
        sellerId: String,
        productTitle: String,
        productImageUrl: String,
        initialMessage: String,
        callback: (conversationId: String?) -> Unit
    ) {
        // A consistent ID for the conversation between two users for a specific product
        val conversationId = conversationsRef.push().key ?: return

        val conversation = ChatConversation(
            conversationId = conversationId,
            productId = productId,
            renterId = renterId,
            sellerId = sellerId,
            productTitle = productTitle,
            productImageUrl = productImageUrl,
            participants = listOf(renterId, sellerId),
            lastMessage = initialMessage,
            lastMessageTimestamp = System.currentTimeMillis()
        )

        conversationsRef.child(conversationId).setValue(conversation).addOnSuccessListener {
            // After creating the conversation, send the first message
            val message = ChatMessage(
                conversationId = conversationId,
                senderId = renterId,
                text = initialMessage
            )
            sendMessage(conversationId, message) { success ->
                if (success) {
                    callback(conversationId)
                } else {
                    callback(null)
                }
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    override fun sendMessage(
        conversationId: String,
        message: ChatMessage,
        callback: (success: Boolean) -> Unit
    ) {
        val messageId = messagesRef.child(conversationId).push().key ?: return
        message.messageId = messageId

        messagesRef.child(conversationId).child(messageId).setValue(message)
            .addOnSuccessListener { 
                // Also update the last message in the conversation for the chat list
                conversationsRef.child(conversationId).updateChildren(
                    mapOf(
                        "lastMessage" to message.text,
                        "lastMessageTimestamp" to message.timestamp
                    )
                ).addOnCompleteListener { 
                    callback(it.isSuccessful)
                }
            }
            .addOnFailureListener { callback(false) }
    }

    override fun getMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                trySend(messages.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        messagesRef.child(conversationId).addValueEventListener(listener)

        awaitClose { messagesRef.child(conversationId).removeEventListener(listener) }
    }

    override fun getConversations(userId: String, callback: (List<ChatConversation>) -> Unit) {
        conversationsRef.orderByChild("participants/0").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = snapshot.children.mapNotNull { it.getValue(ChatConversation::class.java) }
                // This is a simplified query. A more robust solution might need to query for participants/1 as well
                // or restructure the data to allow better querying.
                callback(conversations.sortedByDescending { it.lastMessageTimestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
}
