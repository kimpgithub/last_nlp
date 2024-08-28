package com.example.nlp_project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class UserInfo(
    val name: String,
    val age: Int,
    val gender: String
)

class UserInfoViewModel : ViewModel() {
    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo> get() = _userInfo

    fun saveUserInfo(name: String, age: Int, gender: String) {
        _userInfo.value = UserInfo(name, age, gender)
    }
}
