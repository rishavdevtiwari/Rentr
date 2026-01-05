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
        // First, query to see if a conversation already exists
        conversationsRef
            .orderByChild("productId")
            .equalTo(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val existingConversation = snapshot.children.find { 
                        val convo = it.getValue(ChatConversation::class.java)
                        convo?.participants?.containsKey(renterId) == true && convo.participants.containsKey(sellerId)
                    }

                    if (existingConversation != null) {
                        // Conversation exists, send the message to it and return its ID
                        val existingConvoId = existingConversation.key!!
                        val message = ChatMessage(
                            conversationId = existingConvoId,
                            senderId = renterId,
                            text = initialMessage
                        )
                        sendMessage(existingConvoId, message) { success ->
                            if (success) callback(existingConvoId) else callback(null)
                        }
                    } else {
                        // No conversation exists, create a new one
                        val newConversationId = conversationsRef.push().key ?: return
                        val participants = mapOf(renterId to true, sellerId to true)
                        val conversation = ChatConversation(
                            conversationId = newConversationId,
                            productId = productId,
                            renterId = renterId,
                            sellerId = sellerId,
                            productTitle = productTitle,
                            productImageUrl = productImageUrl,
                            participants = participants,
                            lastMessage = initialMessage,
                            lastMessageTimestamp = System.currentTimeMillis()
                        )
                        conversationsRef.child(newConversationId).setValue(conversation).addOnSuccessListener {
                            val message = ChatMessage(
                                conversationId = newConversationId,
                                senderId = renterId,
                                text = initialMessage
                            )
                            sendMessage(newConversationId, message) { success ->
                                if (success) callback(newConversationId) else callback(null)
                            }
                        }.addOnFailureListener { callback(null) }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
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
        conversationsRef.orderByChild("participants/$userId").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val conversations = snapshot.children.mapNotNull { it.getValue(ChatConversation::class.java) }
                    callback(conversations.sortedByDescending { it.lastMessageTimestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }
}
