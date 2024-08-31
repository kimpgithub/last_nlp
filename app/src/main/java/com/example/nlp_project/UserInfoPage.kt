package com.example.nlp_project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun UserInfoPage(
    viewModel: UserInfoViewModel,
    navController: NavController,  // Add this parameter
    chatViewModel: ChatViewModel
) {
    var age by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("남자") } // Default to "남자"

    Topbar(text = "인적사항", onBackPressed = { null })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp) // Add padding to avoid top app bar overlap
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "※ 채팅 정책 도우미를 위해 기본 사항을 입력해주세요.",
            color = Color.Black,
            fontSize = 12.sp
        )

        CustomOutLinedTextField(age, "나이") { age = it }
        CustomOutLinedTextField(region, "지역") { region = it }

        // Gender Selection
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 14.dp)
                .border(
                    BorderStroke(2.dp, Color(0xFFFF788E)),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("성별", color = Color.Black, modifier = Modifier.padding(16.dp))
            GenderRadioButton(gender) { gender = it }
        }

        Spacer(modifier = Modifier.weight(2f))
        Button(
            onClick = {
                if (age.isNotEmpty() && region.isNotEmpty()) {
                    chatViewModel.saveUserInfo(
                        age.toInt(),
                        gender // Pass the selected gender
                    )
                    navController.navigate("chat_screen")  // Navigate to the chat screen
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(Color(0xFFFF788E))
        ) {
            Text("채팅 시작하기", color = Color.White)
        }
    }
}

@Composable
fun GenderRadioButton(selectedGender: String, onGenderSelected: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButtonWithText(
            text = "남자",
            isSelected = selectedGender == "남자",
            onClick = { onGenderSelected("남자") }
        )
        RadioButtonWithText(
            text = "여자",
            isSelected = selectedGender == "여자",
            onClick = { onGenderSelected("여자") }
        )
    }
}

@Composable
fun RadioButtonWithText(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        androidx.compose.material3.RadioButton(
            selected = isSelected,
            onClick = null, // Make the whole row clickable instead of just the button
            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                selectedColor = Color(0xFFFF788E)
            )
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = text, color = Color.Black)
    }
}

@Composable
private fun CustomOutLinedTextField(value: String, str: String, changeVal: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = { changeVal(it) },
        label = { Text(str, color = Color.Black) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .border(
                BorderStroke(2.dp, Color(0xFFFF788E)), // 테두리 두께와 색상 설정
                shape = RoundedCornerShape(16.dp)     // 모서리 둥글기 설정 (Radius 16px)
            )
            .clip(RoundedCornerShape(16.dp)),
        keyboardOptions = if (str == "나이") {
            KeyboardOptions(keyboardType = KeyboardType.Number) // 숫자 키보드
        } else {
            KeyboardOptions.Default
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        )
    )
}

@Composable
fun Topbar(text: String, onBackPressed:() -> Unit) {
    Column {

        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.startactivityimage1),
                contentDescription = "LOGO",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterVertically)
                    .padding(8.dp)
            )
            Text(
                text = text,
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.weight(3f))

            if (text != "인적사항") {
                IconButton(onClick = { onBackPressed() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Back",
                        tint = Color(0xFFFF788E)
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(16.dp, 0.dp))
    }
}
