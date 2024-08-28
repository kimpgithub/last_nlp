package com.example.nlp_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.example.nlp_project.ui.theme.NLP_ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        setContent {
            NLP_ProjectTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { AppHeader() }
                ) { innerPadding ->
                    ChatPage(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = chatViewModel
                    )
                }
            }
        }
    }
}




