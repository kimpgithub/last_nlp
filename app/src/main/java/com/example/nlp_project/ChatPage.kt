package com.example.nlp_project

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

//ChatPage

@Composable
fun ChatPage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    val messages = viewModel.messages.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.value) { message ->
                when (message) {
                    is ChatMessage.UserMessage -> UserMessageItem(message)
                    is ChatMessage.BotMessage -> BotMessageItem(message)
                }
            }
        }

        MessageInput(onMessageSend = { message ->
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
            }
        })
    }
}

@Composable
fun UserMessageItem(message: ChatMessage.UserMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = message.content,
            color = Color.White,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(12.dp)
        )
    }
}

@Composable
fun BotMessageItem(message: ChatMessage.BotMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        StructuredContentView(answer = message.answer, modifier = Modifier)
    }
}
data class StructuredAnswer(
    val paragraphs: List<String>,
    val links: List<String>
)

@Composable
fun StructuredContentView(answer: StructuredAnswer, modifier: Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        answer.paragraphs.forEach { paragraph ->
            Text(
                text = parseMarkdown(text = paragraph) ,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        if (answer.links.isNotEmpty()) {
            Text(
                text = "참고 링크:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            answer.links.forEach { link ->
                TextButton(onClick = {
                    // Create an intent to open the URL
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                }) {
                    Text(text = link, color = Color.Blue)
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
            modifier = Modifier.padding(16.dp), text = "육아정챗",
            color = Color.White,
            fontSize = 22.sp
        )
    }
}


//마크다운 문법 적용
@Composable
fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val header1Regex = "^# (.*)".toRegex()
        val header2Regex = "^## (.*)".toRegex()
        val header3Regex = "^### (.*)".toRegex()
        val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
        val italicRegex = "\\*(.*?)\\*".toRegex()
        val strikethroughRegex = "~~(.*?)~~".toRegex()
        val linkRegex = "\\[(.*?)\\]\\((.*?)\\)".toRegex()
        val lines = text.lines()

        lines.forEach { line ->
            when {
                header1Regex.containsMatchIn(line) -> {
                    val match = header1Regex.find(line)
                    match?.let {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)) {
                            append(it.groupValues[1] + "\n")
                        }
                    }
                }
                header2Regex.containsMatchIn(line) -> {
                    val match = header2Regex.find(line)
                    match?.let {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                            append(it.groupValues[1] + "\n")
                        }
                    }
                }
                header3Regex.containsMatchIn(line) -> {
                    val match = header3Regex.find(line)
                    match?.let {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                            append(it.groupValues[1] + "\n")
                        }
                    }
                }
                boldRegex.containsMatchIn(line) -> {
                    val match = boldRegex.find(line)
                    match?.let {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(it.groupValues[1])
                        }
                    }
                }
                italicRegex.containsMatchIn(line) -> {
                    val match = italicRegex.find(line)
                    match?.let {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(it.groupValues[1])
                        }
                    }
                }
                strikethroughRegex.containsMatchIn(line) -> {
                    val match = strikethroughRegex.find(line)
                    match?.let {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(it.groupValues[1])
                        }
                    }
                }
                linkRegex.containsMatchIn(line) -> {
                    val match = linkRegex.find(line)
                    match?.let {
                        val (linkText, linkUrl) = it.destructured
                        pushStringAnnotation(tag = "URL", annotation = linkUrl)
                        withStyle(SpanStyle(color = androidx.compose.ui.graphics.Color.Blue, textDecoration = TextDecoration.LineThrough)) {
                            append(linkText)
                        }
                        pop()
                    }
                }
                else -> {
                    append(line + "\n")
                }
            }
        }
    }
}