package com.example.nlp_project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

//ChatViewModel

class ChatViewModel : ViewModel() {
    private val _answer = MutableLiveData<StructuredAnswer>()
    val answer: LiveData<StructuredAnswer> get() = _answer

    private var userAge: Int? = null
    private var userGender: String? = null

    fun saveUserInfo(age: Int, gender: String) {
        userAge = age
        userGender = gender
    }

    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getAnswer(
                    QuestionRequest(
                        question = question,
                        age = userAge,
                        gender = userGender
                    )
                )
                // 응답을 StructuredAnswer 형식으로 변환
                val structuredAnswer = StructuredAnswer(
                    paragraphs = response.answer.split("\n\n"), // 단락 기준으로 분리
                    links = extractLinks(response.answer) // 링크를 추출하는 함수를 정의
                )
                _answer.value = structuredAnswer
            } catch (e: Exception) {
                _answer.value = StructuredAnswer(
                    paragraphs = listOf("Error: ${e.message}"),
                    links = emptyList()
                )
            }
        }
    }

    // 단락에서 링크를 추출하는 함수 (예시)
    private fun extractLinks(text: String): List<String> {
        // 정규식을 사용하여 텍스트에서 URL을 추출
        val urlRegex = "(https?://[\\w-]+(\\.[\\w-]+)+[/#?]?.*)".toRegex()
        return urlRegex.findAll(text).map { it.value }.toList()
    }

}
