package com.example.nlp_project.uiview.userinfo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nlp_project.R


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
