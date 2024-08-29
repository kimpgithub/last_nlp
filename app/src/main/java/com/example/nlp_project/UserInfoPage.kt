package com.example.nlp_project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoPage(
    viewModel: UserInfoViewModel,
    onNext: () -> Unit,
    chatViewModel: ChatViewModel
) {
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") } // Default to "Male"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp) // Add padding to avoid top app bar overlap
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text("※ 채팅 정책 도우미를 위해 기본 사항을 입력해주세요.", color = Color.Black)
        CustomOutLinedTextField(age, "나이") { age = it }
        CustomOutLinedTextField(age, "지역") { age = it }
        CustomOutLinedTextField(age, "연봉") { age = it }

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 14.dp)
                .border(
                    BorderStroke(2.dp, Color(0xFFFF788E)), // 테두리 두께와 색상 설정
                    shape = RoundedCornerShape(16.dp)     // 모서리 둥글기 설정 (Radius 16px)
                )
                .clip(RoundedCornerShape(16.dp)),        // 테두리에 맞게 필드를 자름,
        ) {
            Text("성별", color = Color.Black, modifier = Modifier.padding(16.dp))
            GenderRadioButton(gender) { gender = it }
        }
        Spacer(modifier = Modifier.weight(2f))
        Button(
            onClick = {
                if (age.isNotEmpty()) {
                    chatViewModel.saveUserInfo(
                        age.toInt(),
                        gender
                    )  // Save user info in ChatViewModel
                    onNext()
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(Color(0xFFFF788E))
        ) {
            Text("채팅 시작하기", color = Color.White)
        }
        Spacer(modifier = Modifier.weight(2f))
    }
}

@Composable
private fun GenderRadioButton(gender: String, changeGender: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = gender == "남자",
            onClick = { changeGender("남자") },
            colors = RadioButtonDefaults.colors(Color(0xFFFF788E))
        )
        Text(text = "남자", color = Color.Black, modifier = Modifier.padding(start = 8.dp))

        RadioButton(
            selected = gender == "여자",
            onClick = { changeGender("여자") },
            colors = RadioButtonDefaults.colors(Color(0xFFFF788E)),
            modifier = Modifier.padding(start = 16.dp)
        )
        Text(text = "여자", color = Color.Black, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun CustomOutLinedTextField(age: String, str: String, changeVal: (String) -> Unit) {
    TextField(
        value = age,
        onValueChange = { changeVal(age) },
        label = { Text(str, color = Color.Black) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .border(
                BorderStroke(2.dp, Color(0xFFFF788E)), // 테두리 두께와 색상 설정
                shape = RoundedCornerShape(16.dp)     // 모서리 둥글기 설정 (Radius 16px)
            )
            .clip(RoundedCornerShape(16.dp)),        // 테두리에 맞게 필드를 자름,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        )
    )
}
