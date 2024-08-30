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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.foundation.text.ClickableText

//ChatPage

@Composable
fun ChatPage(modifier: Modifier = Modifier, viewModel: ChatViewModel, onBackPressed: () -> Unit) {
    val messages = viewModel.messages.collectAsState()
    Log.d("ChatPage", "Rendering ChatPage with messages: ${messages.value.size}")
    var userInfoCollected by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        Topbar(text = "채팅 정책 서비스", onBackPressed = { null })

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.value) { message ->
                when (message) {
                    is ChatMessage.UserMessage -> {
                        Log.d("ChatPage", "UserMessage: ${message.content}")
                        ChatBubble(message = message.content, isUser = true)
                    }

                    is ChatMessage.BotMessage -> {
                        Log.d(
                            "ChatPage",
                            "BotMessage: ${message.answer.paragraphs.joinToString("\n")}"
                        )
                        ChatBubble(
                            message = parseMarkdown(message.answer.paragraphs.joinToString("\n")),
                            isUser = false
                        )
                    }
                }
            }
        }

        MessageInput(onMessageSend = { message ->
            if (message.isNotEmpty()) {
                Log.d("ChatPage", "Message sent: $message")
                viewModel.sendMessage(message)
            }
        })
    }
}

@Composable
fun ChatBubble(message: AnnotatedString, isUser: Boolean) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            ClickableText(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(color = if (isUser) Color.White else Color.Black),
                onClick = { offset ->
                    message.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(intent)
                        }
                }
            )
        }
    }
}

@Composable
fun ChatBubble(message: String, isUser: Boolean) {
    ChatBubble(message = AnnotatedString(message), isUser = isUser)
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
                text = parseMarkdown(text = paragraph),
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
            onValueChange = {
                Log.d("MessageInput", "Message input changed: $it")
                message = it
            },
            placeholder = { Text("궁금한 내용을 입력하세요") } // 단순히 placeholder를 설정
        )
        IconButton(onClick = {
            if (message.isNotEmpty()) {
                Log.d("MessageInput", "Sending message: $message")
                onMessageSend(message)
                message = ""
            }
        }) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
        }
    }
}

@Composable
fun AppHeader(onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(onClick = { onBackPressed() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "육아정챗",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

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
        val colonSeparatedRegex = "-\\s\\*\\*(.*?)\\*\\*:\\s*(.*)".toRegex() // ':' 뒤 내용을 포함하는 패턴
        val lines = text.lines()

        lines.forEachIndexed { index, line ->
            when {
                header1Regex.containsMatchIn(line) -> {
                    val match = header1Regex.find(line)
                    match?.let {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)) {
                            append(it.groupValues[1])
                        }
                    }
                }

                header2Regex.containsMatchIn(line) -> {
                    val match = header2Regex.find(line)
                    match?.let {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                            append(it.groupValues[1])
                        }
                    }
                }

                header3Regex.containsMatchIn(line) -> {
                    val match = header3Regex.find(line)
                    match?.let {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                            append(it.groupValues[1])
                        }
                    }
                }

                colonSeparatedRegex.containsMatchIn(line) -> {
                    val match = colonSeparatedRegex.find(line)
                    match?.let {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(it.groupValues[1] + ": ")
                        }
                        append(it.groupValues[2])
                    }
                }

                boldRegex.containsMatchIn(line) -> {
                    val matches = boldRegex.findAll(line)
                    var lastEnd = 0
                    matches.forEach { match ->
                        append(line.substring(lastEnd, match.range.first))
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(match.groupValues[1])
                        }
                        lastEnd = match.range.last + 1
                    }
                    append(line.substring(lastEnd))
                }

                italicRegex.containsMatchIn(line) -> {
                    val matches = italicRegex.findAll(line)
                    var lastEnd = 0
                    matches.forEach { match ->
                        append(line.substring(lastEnd, match.range.first))
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(match.groupValues[1])
                        }
                        lastEnd = match.range.last + 1
                    }
                    append(line.substring(lastEnd))
                }

                strikethroughRegex.containsMatchIn(line) -> {
                    val matches = strikethroughRegex.findAll(line)
                    var lastEnd = 0
                    matches.forEach { match ->
                        append(line.substring(lastEnd, match.range.first))
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(match.groupValues[1])
                        }
                        lastEnd = match.range.last + 1
                    }
                    append(line.substring(lastEnd))
                }

                linkRegex.containsMatchIn(line) -> {
                    val matches = linkRegex.findAll(line)
                    var lastEnd = 0
                    matches.forEach { match ->
                        append(line.substring(lastEnd, match.range.first))
                        val (linkText, linkUrl) = match.destructured
                        pushStringAnnotation(tag = "URL", annotation = linkUrl)
                        withStyle(
                            SpanStyle(
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(linkText)
                        }
                        pop()
                        lastEnd = match.range.last + 1
                    }
                    append(line.substring(lastEnd))
                }

                else -> {
                    append(line)
                }
            }

            if (index != lines.lastIndex) {
                append("\n") // 줄바꿈을 각 라인 사이에 추가
            }
        }
    }
}