package com.example.nlp_project

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _answer = MutableLiveData<String>()
    val answer: LiveData<String> get() = _answer

    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getAnswer(QuestionRequest(question))
                Log.d("ChatViewModel", "Response: $response")
                _answer.value = response.answer
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error: ${e.message}")
                _answer.value = "Error: ${e.message}"
            }
        }
    }
}