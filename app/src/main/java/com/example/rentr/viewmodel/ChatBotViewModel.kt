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

    // --- CONFIGURE FOR GROQ ---
    private val config = OpenAIConfig(
        token = BuildConfig.GROQ_API_KEY,
        // TRICK: Point to Groq's server instead of OpenAI
        host = OpenAIHost(baseUrl = "https://api.groq.com/openai/v1/"),
        timeout = Timeout(socket = 60.seconds)
    )

    private val openAI = OpenAI(config)

    fun sendMessage(userMessageText: String) {
        // 1. Add User Message to UI
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + UiMessage(userMessageText, true)
        )

        viewModelScope.launch {
            try {
                // 2. Convert history for the API
                val apiMessages = _uiState.value.messages.map {
                    ChatMessage(
                        role = if (it.isUser) ChatRole.User else ChatRole.Assistant,
                        content = it.text
                    )
                }

                // 3. Create Request using Llama 3
                val request = ChatCompletionRequest(
                    // Groq supports "llama3-8b-8192" or "mixtral-8x7b-32768"
                    model = ModelId("llama-3.1-8b-instant"),
                    messages = apiMessages
                )

                // 4. Get Response
                val completion = openAI.chatCompletion(request)
                val responseText = completion.choices.first().message.content ?: "No response"

                // 5. Update UI
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + UiMessage(responseText, false)
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + UiMessage("Error: ${e.message}", false)
                )
            }
        }
    }
}

// Simple UI classes
data class UiMessage(val text: String, val isUser: Boolean)
data class ChatbotUiState(val messages: List<UiMessage> = emptyList())