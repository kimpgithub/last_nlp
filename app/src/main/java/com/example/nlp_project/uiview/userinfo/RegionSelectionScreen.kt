package com.example.nlp_project.uiview.userinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nlp_project.data.Region
import com.example.nlp_project.data.regions


@Composable
fun RegionSelectionScreen(
    selectedRegions: Set<String>,
    onRegionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTopRegion by remember { mutableStateOf<Region?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "선호 지역",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 상위 지역 그리드
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)  // 최대 높이 설정
        ) {
            items(regions) { region ->
                RegionChip(
                    region = region.name,
                    isSelected = region == selectedTopRegion,
                    onSelected = { selectedTopRegion = region }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 하위 지역 그리드
        selectedTopRegion?.let { region ->
            if (region.subregions.isNotEmpty()) {
                Text(
                    text = "${region.name}의 하위 지역",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 80.dp)  // 최대 높이 설정
                ) {
                    items(region.subregions) { subregion ->
                        RegionChip(
                            region = subregion.name,
                            isSelected = subregion.name in selectedRegions,
                            onSelected = { onRegionSelected(subregion.name) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SelectedRegionsSection(
            selectedRegions = selectedRegions,
            onRemoveRegion = { region ->
                onRegionSelected(region)
            },
            onClearAll = {
                selectedRegions.forEach { region ->
                    onRegionSelected(region)
                }
            }
        )
    }
}