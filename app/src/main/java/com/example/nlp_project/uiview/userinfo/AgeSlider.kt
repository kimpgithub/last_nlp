package com.example.nlp_project.uiview.userinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AgeSlider(
    age: String,
    onAgeChange: (Int) -> Unit
) {
    // age가 빈 문자열이면 기본값 10을 사용
    val initialAge = age.toFloatOrNull() ?: 10f

    // Slider의 값을 관리할 상태를 기억
    val sliderPosition = remember { mutableStateOf(initialAge) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "나이",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Slider(
            value = sliderPosition.value,
            onValueChange = {
                sliderPosition.value = it
                onAgeChange(it.toInt())
            },
            valueRange = 10f..100f,
            steps = 0,  // 1씩 증가
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF788E),  // 썸 색상
                activeTrackColor = Color(0xFFFF788E),  // 활성 상태 트랙 색상
                inactiveTrackColor = Color.LightGray  // 비활성 상태 트랙 색상
            ),
            modifier = Modifier.fillMaxWidth()
        )
        // 선택된 나이를 표시
        Text(
            text = sliderPosition.value.toInt().toString(),
            fontSize = 20.sp,
            color = Color(0xFFFF788E), // 강조 색상
            modifier = Modifier
                .background(Color.White)
                .padding(top = 8.dp)
        )
    }
}
