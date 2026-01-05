package com.example.rentr.model

import com.google.firebase.database.Exclude

/**
 * Represents a single conversation thread in the user's chat list.
 * We only store IDs here to avoid stale data. The product and user info
 * will be fetched dynamically when displaying the conversation list.
 */
data class ChatConversation(
    var conversationId: String = "",
    val productId: String = "",
    val sellerId: String = "",
    val renterId: String = "",
    var lastMessage: String = "",
    var lastMessageTimestamp: Long = 0L,
    val participants: Map<String, Boolean> = emptyMap(),
    var unreadCount: Int = 0
) {
    // Firebase requires a no-argument constructor
    constructor() : this("", "", "", "", "", 0L, emptyMap(), 0)

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "conversationId" to conversationId,
            "productId" to productId,
            "sellerId" to sellerId,
            "renterId" to renterId,
            "lastMessage" to lastMessage,
            "lastMessageTimestamp" to lastMessageTimestamp,
            "participants" to participants,
            "unreadCount" to unreadCount
        )
    }
}
