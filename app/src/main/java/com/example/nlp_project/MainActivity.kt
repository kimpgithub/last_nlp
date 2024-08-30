package com.example.nlp_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nlp_project.ui.theme.NLP_ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        val userInfoViewModel = ViewModelProvider(this)[UserInfoViewModel::class.java]

        setContent {
            NLP_ProjectTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "start_screen") {
                    composable("start_screen") {
                        StartScreen(navController)
                    }
                    composable("user_info_screen") {
                        UserInfoPage(
                            viewModel = userInfoViewModel,
                            navController = navController,
                            chatViewModel = chatViewModel
                        )
                    }
                    composable("chat_screen") {
                        ChatPage(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = chatViewModel,
                            navController = navController // Pass the navController here
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun StartScreen(navController: NavController) {
        Column(modifier = Modifier
            .fillMaxSize()
            .clickable { navController.navigate("user_info_screen") }) {

            Spacer(modifier = Modifier.weight(2f))
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "곧 태어날 아이와 가족을 위한 \n" +
                            "특별한 혜택을 물어보세요",
                    fontSize = 16.sp,
                    color = Color(0xffff788E),
                    textAlign = TextAlign.Center
                )
                Image(
                    painter = painterResource(id = R.drawable.startactivityimage1),
                    contentDescription = "LOGO",
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                )
            }

            Spacer(modifier = Modifier.weight(2f))
            Box(modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd) {
                Image(
                    painter = painterResource(id = R.drawable.startactivityimage5),
                    contentDescription = "TALKBALLON",
                    modifier = Modifier
                        .size(280.dp)
                )
            }

            Spacer(modifier = Modifier.weight(.5f))
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Image(
                    painter = painterResource(id = R.drawable.birthpolichat_logo),
                    contentDescription = "CHUNG",
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = "육아정챗",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.weight(.5f))
        }
    }
}
