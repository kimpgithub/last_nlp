package com.example.nlp_project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private var userGender: String? = null

    fun saveUserInfo(age: Int, gender: String) {
        userAge = age
        userGender = gender
    }

    fun sendMessage(question: String) {
        viewModelScope.launch {
            _messages.value += ChatMessage.UserMessage(question)
            try {
                val response = RetrofitInstance.api.getAnswer(
                    QuestionRequest(
                        question = question,
                        age = userAge,
                        gender = userGender
                    )
                )
                val structuredAnswer = StructuredAnswer(
                    paragraphs = response.answer.split("\n\n"),
                    links = extractLinks(response.answer)
                )
                _messages.value += ChatMessage.BotMessage(structuredAnswer)
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