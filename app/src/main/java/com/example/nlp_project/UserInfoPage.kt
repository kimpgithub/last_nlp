package com.example.nlp_project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun UserInfoPage(viewModel: UserInfoViewModel, onNext: () -> Unit, chatViewModel: ChatViewModel) {
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") } // Default to "Male"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp) // Add padding to avoid top app bar overlap
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text("Gender", modifier = Modifier.padding(top = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = gender == "Male",
                onClick = { gender = "Male" },
                colors = RadioButtonDefaults.colors()
            )
            Text(text = "Male", modifier = Modifier.padding(start = 8.dp))

            RadioButton(
                selected = gender == "Female",
                onClick = { gender = "Female" },
                colors = RadioButtonDefaults.colors(),
                modifier = Modifier.padding(start = 16.dp)
            )
            Text(text = "Female", modifier = Modifier.padding(start = 8.dp))
        }

        Button(
            onClick = {
                if (age.isNotEmpty()) {
                    val ageInt = age.toInt()
                    chatViewModel.saveUserInfo(ageInt, gender)  // Save user info in ChatViewModel
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Next")
        }
    }
}
