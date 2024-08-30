package com.example.nlp_project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

//ChatViewModel
class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(ChatMessage.BotMessage(
        StructuredAnswer(listOf("반갑습니다, 어떤 출산/보육 정책이 궁금하신가요?"), emptyList())
    )))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private var userAge: Int? = null

    fun saveUserInfo(age: Int, gender: String) {
        userAge = age
    }

    fun sendMessage(question: String) {
        viewModelScope.launch {
            _messages.value += ChatMessage.UserMessage(question)
            try {
                val response = RetrofitInstance.api.getAnswer(
                    QuestionRequest(
                        question = question,
                        age = userAge
                    )
                )

                if (response.isSuccessful) {
                    response.body()?.let { structuredResponse ->
                        val structuredAnswer = StructuredAnswer(
                            paragraphs = structuredResponse.answer.split("\n\n"),
                            links = extractLinks(structuredResponse.answer)
                        )
                        _messages.value += ChatMessage.BotMessage(structuredAnswer)
                    }
                } else {
                    _messages.value += ChatMessage.BotMessage(
                        StructuredAnswer(
                            paragraphs = listOf("Error: ${response.message()}"),
                            links = emptyList()
                        )
                    )
                }
            } catch (e: Exception) {
                _messages.value += ChatMessage.BotMessage(
                    StructuredAnswer(
                        paragraphs = listOf("Error: ${e.message}"),
                        links = emptyList()
                    )
                )
            }
        }
    }

    private fun extractLinks(text: String): List<String> {
        val urlRegex = "(https?://[\\w-]+(\\.[\\w-]+)+[/#?]?.*)".toRegex()
        return urlRegex.findAll(text).map { it.value }.toList()
    }
}

sealed class ChatMessage {
    data class UserMessage(val content: String) : ChatMessage()
    data class BotMessage(val answer: StructuredAnswer) : ChatMessage()
}

data class StructuredAnswer(
    val paragraphs: List<String>,
    val links: List<String>
)
