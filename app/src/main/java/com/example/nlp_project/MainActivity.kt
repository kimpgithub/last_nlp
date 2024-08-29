package com.example.nlp_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.nlp_project.ui.theme.NLP_ProjectTheme
import kotlinx.coroutines.delay

//MainActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        val userInfoViewModel = ViewModelProvider(this)[UserInfoViewModel::class.java]

        setContent {
            NLP_ProjectTheme {
                var isStartActivity by remember { mutableStateOf(true) }
                //시작화면은 3초 유지 or 클릭하면 넘어가게
                if (isStartActivity) {
                    StartScreen { isStartActivity = false }

                    LaunchedEffect(key1 = true) {
                        delay(3000)
                        isStartActivity = false
                    }

                } else {
                    var userInfoCollected by remember { mutableStateOf(false) }

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize(),
                        topBar = { AppHeader() }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .background(color = Color.White)
                        ) {
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

    @Composable
    private fun StartScreen(changeScreen: () -> Unit) {
        Column(modifier = Modifier
            .fillMaxSize()
            .clickable { changeScreen() }) {

            Spacer(modifier = Modifier.weight(2f))
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "곧 태어날 아이와 가족을 위한 \n" +
                            "특별한 혜택을 물어보세요",
                    fontSize = 20.sp,
                    color = Color(0xffff788E),
                    textAlign = TextAlign.Center
                )
                Image(
                    painter = painterResource(id = R.drawable.startactivityimage1),
                    contentDescription = "LOGO",
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.weight(2f))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(160.dp))
                Image(
                    painter = painterResource(id = R.drawable.startactivityimage5),
                    contentDescription = "TALKBALLON",
                    modifier = Modifier
                        .size(400.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Image(
                    painter = painterResource(id = R.drawable.startactivityimage4),
                    contentDescription = "CHUNG"
                )
                Text(
                    text = "육아정챗",
                    fontSize = 24.sp
                )
            }
        }
    }
}