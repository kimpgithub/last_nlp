package com.example.nlp_project.uiview.userinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun SelectedRegionsSection(
    selectedRegions: Set<String>,
    onRemoveRegion: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("선택한 지역", fontWeight = FontWeight.Bold)
            TextButton(onClick = onClearAll) {
                Text("전체삭제", color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 선택된 지역을 3개씩 묶어서 Row로 배치
        selectedRegions.chunked(3).forEach { regionRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                regionRow.forEach { region ->
                    AssistChip(
                        onClick = { onRemoveRegion(region) },
                        label = { Text(region, color = Color.Black) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove"
                            )
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
