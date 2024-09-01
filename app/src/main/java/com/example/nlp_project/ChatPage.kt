package com.example.nlp_project

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavController
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    navController: NavController,
    onBackPressed: () -> Unit = { navController.popBackStack() }
) {
    val messages = viewModel.messages.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var scrollToMessage by remember { mutableStateOf(false) } // Control scroll action

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp, 0.dp)
    ) {
        Topbar(text = "정챗 도우미", onBackPressed = onBackPressed)

        LaunchedEffect(messages.value.size, scrollToMessage) {
            if (scrollToMessage) {
                listState.animateScrollToItem(messages.value.size)
                scrollToMessage = false
            }
        }

        LazyColumn(
            state = listState, // Pass the listState to control scrolling
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Adding cards at the top of the chat page
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoCard(title = "정책 안내", description = "출산/보육 정책을 안내합니다.", onClick = {
                        viewModel.sendMessage("정책 안내")
                    }, modifier = Modifier.weight(1f))

                    InfoCard(title = "신청 방법", description = "지원 신청 방법을 알아보세요.", onClick = {
                        viewModel.sendMessage("신청 방법")
                    }, modifier = Modifier.weight(1f))

                    InfoCard(title = "지원 대상", description = "지원 대상 여부를 확인하세요.", onClick = {
                        viewModel.sendMessage("지원 대상")
                    }, modifier = Modifier.weight(1f))
                }
            }

            items(messages.value) { message ->
                when (message) {
                    is ChatMessage.UserMessage -> {
                        Log.d("ChatPage", "UserMessage: ${message.content}")
                        ChatBubble(message = message.content, isUser = true)
                    }

                    is ChatMessage.BotMessage -> {
                        Log.d("ChatPage", "BotMessage received from server: ${message.fromServer}")
                        Log.d("ChatPage", "Policies: ${message.answer.policies}")

                        ChatBubble(
                            message = parseMarkdown(message.answer.paragraphs.joinToString("\n")),
                            isUser = false
                        )

                        // 서버 응답에 포함된 policies 리스트를 SmallCardRow에 전달
                        if (message.fromServer && message.answer.policies.isNotEmpty()) {
                            Log.d("ChatPage", "Displaying SmallCardRow")
                            SmallCardRow(
                                policies = message.answer.policies,
                                viewModel = viewModel,
                                onCardClick = {
                                    scrollToMessage = true
                                } // Trigger scroll on card click
                            )
                        } else {
                            Log.d("ChatPage", "No policies to display")
                        }
                    }
                }
            }
        }

        MessageInput(onMessageSend = { message ->
            if (message.isNotEmpty()) {
                Log.d("ChatPage", "MessageInput: $message")
                viewModel.sendMessage(message)
            }
        })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmallCardRow(
    policies: List<String>,
    viewModel: ChatViewModel,
    onCardClick: () -> Unit // Callback to trigger scroll on card click
) {
    Log.d("SmallCardRow", "Rendering SmallCardRow with policies: $policies")

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        policies.forEach { policy ->
            SmallCard(
                title = policy,
                viewModel = viewModel,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                onClick = {
                    viewModel.sendMessage("'$policy'에 대해 자세히 알려줘") // Trigger message send
                    onCardClick() // Trigger scroll when a card is clicked
                }
            )
        }
    }
}


@Composable
fun SmallCard(
    title: String,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .clickable(onClick = onClick) // 이곳에서 viewModel.sendMessage를 호출하지 않음
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(Color(0xFFFF788E))
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White
                ),
                modifier = Modifier.padding(8.dp)
            )
        }
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
                    color = if (isUser) Color.White else Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp, // 두께를 설정
                    color = if (isUser) Color(0xFFFF788E) else Color.LightGray,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            ClickableText(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(color = if (isUser) Color.Black else Color.Black),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(onMessageSend: (String) -> Unit) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f).padding(8.dp),
            value = message,
            onValueChange = {
                Log.d("MessageInput", "Message input changed: $it")
                message = it
            },
            placeholder = { Text("궁금한 내용을 입력하세요") }, // 단순히 placeholder를 설정
            shape = CircleShape,  // Circle 모양으로 변경
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFFFF788E),
                unfocusedBorderColor = Color(0xFFFF788E)
            )
        )

        IconButton(onClick = {
            if (message.isNotEmpty()) {
                Log.d("MessageInput", "Sending message: $message")
                onMessageSend(message)
                message = ""
            }
        }) {
            Icon(imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = Color(0xFFFF788E))
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
        val colonSeparatedRegex =
            "-\\s\\*\\*(.*?)\\*\\*:\\s*(.*)".toRegex() // ':' 뒤 내용을 포함하는 패턴
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

