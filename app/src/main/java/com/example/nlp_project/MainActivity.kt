package com.example.nlp_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.nlp_project.ui.theme.NLP_ProjectTheme

//MainActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        val userInfoViewModel = ViewModelProvider(this)[UserInfoViewModel::class.java]

        setContent {
            NLP_ProjectTheme {
                var userInfoCollected by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { AppHeader() }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        if (userInfoCollected) {
                            ChatPage(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = chatViewModel
                            )
                        } else {
                            UserInfoPage(
                                viewModel = userInfoViewModel,
                                onNext = {
                                    userInfoCollected = true
                                    chatViewModel.sendMessage("User information collected.")
                                },
                                chatViewModel = chatViewModel  // Pass chatViewModel here
                            )
                        }
                    }
                }
            }
        }
    }
}