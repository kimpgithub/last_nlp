package com.example.nlp_project

import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

//ChatPage

@Composable
fun ChatPage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    val answer by viewModel.answer.observeAsState(StructuredAnswer(emptyList(), emptyList()))
    val messages = remember { mutableStateListOf<Pair<String, Boolean>>() } // Pair(메시지, isUser)

    // 첫 번째 환영 메시지를 추가
    remember {
        messages.add(Pair("반갑습니다, 어떤 출산/보육 정책이 궁금하신가요?", false))
    }


    // 사용자가 메시지를 입력했을 때 호출되는 함수
    fun addMessage(message: String, isUser: Boolean) {
        messages.add(Pair(message, isUser))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Set background color
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { (text, isUser) ->
                if (isUser) {
                    // 사용자 질문을 오른쪽에 정렬
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = text,
                            color = Color.White,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(12.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                } else {
                    // 답변을 왼쪽에 정렬
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        StructuredContentView(answer = answer, modifier = Modifier)
                    }
                }
            }
        }

        MessageInput(onMessageSend = { message ->
            if (message.isNotEmpty()) {
                addMessage(message, true) // 질문을 추가
                viewModel.sendMessage(message)
            }
        })
    }

    // Answer가 업데이트되었을 때만 답변 추가
    if (answer.paragraphs.isNotEmpty() && messages.none { it.first == answer.paragraphs.joinToString { " " } && !it.second }) {
        messages.add(Pair(answer.paragraphs.joinToString { " " }, false)) // 답변을 메시지 리스트에 추가
    }
}


data class StructuredAnswer(
    val paragraphs: List<String>,
    val links: List<String>
)

@Composable
fun StructuredContentView(answer: StructuredAnswer, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(answer.paragraphs) { paragraph ->
            Text(
                text = paragraph,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        if (answer.links.isNotEmpty()) {
            item {
                Text(
                    text = "참고 링크:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(answer.links) { link ->
                TextButton(onClick = { /* 링크 처리 로직 */ }) {
                    Text(text = link, color = androidx.compose.ui.graphics.Color.Blue)
                }
            }
        }
    }

}


@Composable
fun MessageInput(onMessageSend: (String) -> Unit) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            onValueChange = { message = it },
            placeholder = { Text("궁금한 내용을 입력하세요") } // 단순히 placeholder를 설정
        )
        IconButton(onClick = {
            if (message.isNotEmpty()) { // 메시지가 있을 때만 전송
                onMessageSend(message)
                message = ""
            }
        }) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
        }
    }
}

@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier.padding(16.dp), text = "출산 정책 Chat Bot",
            color = Color.White,
            fontSize = 22.sp
        )
    }
}