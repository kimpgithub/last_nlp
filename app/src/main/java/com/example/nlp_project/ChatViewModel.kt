package com.example.nlp_project

import android.util.Log
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
            Log.d("ChatViewModel", "User message sent: $question")
            try {
                val response = RetrofitInstance.api.getAnswer(
                    QuestionRequest(
                        question = question,
                        age = userAge
                    )
                )

                if (response.isSuccessful) {
                    Log.d("ChatViewModel", "Response received successfully")
                    response.body()?.let { structuredResponse ->
                        Log.d("ChatViewModel", "Structured Response: $structuredResponse")

                        val structuredAnswer = StructuredAnswer(
                            paragraphs = structuredResponse.answer.paragraphs ?: emptyList(),  // null이면 빈 리스트로 설정
                            links = structuredResponse.answer.links ?: emptyList(),  // null이면 빈 리스트로 설정
                            policies = structuredResponse.answer.policies  // 기본값이 이미 설정되어 있어 null 처리 불필요
                        )

                        Log.d("ChatViewModel", "Parsed StructuredAnswer: $structuredAnswer")

                        _messages.value += ChatMessage.BotMessage(
                            answer = structuredAnswer,
                            fromServer = structuredResponse.fromServer // fromServer 플래그 설정
                        )
                    }
                } else {
                    Log.e("ChatViewModel", "Error response: ${response.message()}")
                    _messages.value += ChatMessage.BotMessage(
                        StructuredAnswer(
                            paragraphs = listOf("Error: ${response.message()}"),
                            links = emptyList()
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception occurred: ${e.message}")
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
    data class BotMessage(
        val answer: StructuredAnswer,
        val fromServer: Boolean = false // 서버에서 온 응답인지 확인하는 플래그 추가
    ) : ChatMessage()
}

data class StructuredAnswer(
    val paragraphs: List<String>,
    val links: List<String>,
    val policies: List<String> = emptyList() // 기본값으로 빈 리스트를 설정
)
