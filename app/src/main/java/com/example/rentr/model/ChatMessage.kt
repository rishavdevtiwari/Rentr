package com.example.rentr.model

import com.google.firebase.database.Exclude
data class ChatMessage(
    var messageId: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    constructor() : this("", "", "", "", 0L, false)
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "messageId" to messageId,
            "conversationId" to conversationId,
            "senderId" to senderId,
            "text" to text,
            "timestamp" to timestamp,
            "isRead" to isRead,
        )
    }
}
