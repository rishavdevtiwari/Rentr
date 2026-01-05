package com.example.rentr.model

import com.google.firebase.database.Exclude

data class ChatConversation(
    var conversationId: String = "",
    val productId: String = "",
    val productTitle: String = "",
    val productImageUrl: String = "",
    val sellerId: String = "",
    val renterId: String = "",
    var lastMessage: String = "",
    var lastMessageTimestamp: Long = 0L,
    val participants: List<String> = emptyList(),
    var unreadCount: Int = 0
) {
    // Firebase requires a no-argument constructor
    constructor() : this("", "", "", "", "", "", "", 0L, emptyList(), 0)

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "conversationId" to conversationId,
            "productId" to productId,
            "productTitle" to productTitle,
            "productImageUrl" to productImageUrl,
            "sellerId" to sellerId,
            "renterId" to renterId,
            "lastMessage" to lastMessage,
            "lastMessageTimestamp" to lastMessageTimestamp,
            "participants" to participants,
            "unreadCount" to unreadCount
        )
    }
}
