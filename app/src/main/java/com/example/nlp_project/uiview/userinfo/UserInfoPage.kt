package com.example.nlp_project.uiview.userinfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.UserInfoViewModel

@Composable
fun UserInfoPage(
    viewModel: UserInfoViewModel,
    navController: NavController,
    chatViewModel: ChatViewModel
) {
    var age by remember { mutableStateOf("") }
    var selectedRegions by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Topbar는 고정
        Topbar(text = "인적사항", onBackPressed = { navController.popBackStack() })

        // LazyColumn을 Topbar 아래에 배치
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "※ 채팅 정책 도우미를 위해 기본 사항을 입력해주세요.",
                    color = Color.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 나이 입력 부분을 Slider로 변경
                AgeSlider(age) { newAge ->
                    age = newAge.toString()
                }
//                CustomOutLinedTextField(age, "나이") { age = it }
            }

            item {
                // 지역 선택 UI
                RegionSelectionScreen(
                    selectedRegions = selectedRegions,
                    onRegionSelected = { region ->
                        selectedRegions = if (selectedRegions.contains(region)) {
                            selectedRegions - region
                        } else {
                            selectedRegions + region
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            item {
                Button(
                    onClick = {
                        if (age.isNotEmpty() && selectedRegions.isNotEmpty()) {
                            chatViewModel.saveUserInfo(
                                age.toInt(),
                                selectedRegions.first()
                            )
                            navController.navigate("chat_screen")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth() // 버튼을 너비에 맞게 채웁니다.
                        .padding(horizontal = 16.dp), // 양옆에 패딩을 추가합니다.
                    colors = ButtonDefaults.buttonColors(Color(0xFFFF788E))
                ) {
                    Text("채팅 시작하기", color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RegionChip(
    region: String,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onSelected() },
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) Color(0xffff788E) else Color.White, // 선택된 경우 배경색을 0xffff788E로 설정
        border = BorderStroke(1.dp, Color.Black) // 검은색 테두리 추가
    ) {
        Text(
            text = region,
            color = if (isSelected) Color.White else Color.Black, // 선택된 경우 흰색 글씨, 그렇지 않으면 검은색 글씨
            modifier = Modifier.padding(8.dp),
            fontSize = 14.sp
        )
    }
}


@Composable
fun CustomOutLinedTextField(value: String, str: String, changeVal: (String) -> Unit) {
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
