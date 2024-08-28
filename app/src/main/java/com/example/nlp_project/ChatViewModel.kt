package com.example.nlp_project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

//ChatViewModel

class ChatViewModel : ViewModel() {
    private val _answer = MutableLiveData<String>()
    val answer: LiveData<String> get() = _answer

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
                _answer.value = response.answer
            } catch (e: Exception) {
                _answer.value = "Error: ${e.message}"
            }
        }
    }
}
