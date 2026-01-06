package com.example.rentr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.example.rentr.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class ChatbotViewModel : ViewModel() {

    private val _uiState: MutableStateFlow<ChatbotUiState> = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    // --- 1. DEFINE THE BRAIN (System Prompt) ---
    // This tells the AI who it is and how Rentr works.
    private val systemInstruction = """
        You are the official AI Assistant for 'Rentr', Nepal's premier peer-to-peer rental marketplace.
        Your goal is to help users rent items, list their own products, and stay safe. But also answer other questions with users have it unrelated to the app as well and for questions unrealted to the app appear more friendly.

        --- APP KNOWLEDGE BASE ---
        1. **Navigation**: 
           - **Home**: Browse categories (Vehicles, Electronics, Trekking Gear) or search.
           - **Rent**: Click a product -> Select Days -> 'Request to Rent'.
           - **List Item**: Go to Profile -> 'Add Listing'. (Requires Verification!)
        
        2. **Rules for Listing Items**:
           - User MUST be **Verified** (KYC Complete) to list items.
           - If a user asks "Why can't I list?", tell them to check their KYC status in Profile.
           - Requirements: 4 to 7 photos, Title, Description, and Price in NPR.

        3. **Payments**:
           - We support **Cash on Delivery** and **Khalti**.
           - Do not mention PayPal, Stripe, or Dollars. Use NPR (Rs.).

        4. **Safety**:
           - If a user sees a scam, guide them to use the **Flag** button on the product page.
           - Verification requires: Citizenship (Front/Back), Pan Card, and PP Photo.

        --- BEHAVIOR ---
        - Keep answers short and friendly.
        - If the user says "Hi", welcome them to Rentr.
    """.trimIndent()

    // --- CONFIGURE FOR GROQ ---
    private val config = OpenAIConfig(
        token = BuildConfig.GROQ_API_KEY,
        host = OpenAIHost(baseUrl = "https://api.groq.com/openai/v1/"),
        timeout = Timeout(socket = 60.seconds)
    )

    private val openAI = OpenAI(config)

    fun sendMessage(userMessageText: String) {
        // 2. Add User Message to UI immediately
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + UiMessage(userMessageText, true)
        )

        viewModelScope.launch {
            try {
                // 3. Prepare the conversation history
                // STEP A: Create the System Message (The hidden instruction)
                val systemMessage = ChatMessage(
                    role = ChatRole.System,
                    content = systemInstruction
                )

                // STEP B: Convert previous UI messages to API messages
                val historyMessages = _uiState.value.messages.map {
                    ChatMessage(
                        role = if (it.isUser) ChatRole.User else ChatRole.Assistant,
                        content = it.text
                    )
                }

                // STEP C: Combine them (System Prompt FIRST, then History)
                val finalMessageList = listOf(systemMessage) + historyMessages

                // 4. Create Request
                val request = ChatCompletionRequest(
                    model = ModelId("llama-3.1-8b-instant"),
                    messages = finalMessageList
                )

                // 5. Get Response
                val completion = openAI.chatCompletion(request)
                val responseText = completion.choices.first().message.content ?: "No response"

                // 6. Update UI with Bot Response
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + UiMessage(responseText, false)
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + UiMessage("Error: ${e.localizedMessage}", false)
                )
            }
        }
    }
}

// Simple UI classes
data class UiMessage(val text: String, val isUser: Boolean)
data class ChatbotUiState(val messages: List<UiMessage> = emptyList())