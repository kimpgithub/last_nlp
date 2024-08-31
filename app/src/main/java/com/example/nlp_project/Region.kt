package com.example.nlp_project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Region(val name: String, val subregions: List<Region> = emptyList())

val regions = listOf(
    Region(
        "강원특별자치도",
        listOf(
            Region("고성군"),
            Region("영월군"),
            Region("인제군"),
            Region("정선군"),
            Region("태백시"),
            Region("평창군"),
            Region("화천군"),
            Region("횡성군")
        )
    ),
    Region(
        "경기도",
        listOf(
            Region("고양시"),
            Region("과천시"),
            Region("광주시"),
            Region("군포시"),
            Region("동두천시"),
            Region("부천시"),
            Region("성남시"),
            Region("수원시"),
            Region("시흥시"),
            Region("안산시"),
            Region("안성시"),
            Region("안양시"),
            Region("양평군"),
            Region("여주시"),
            Region("이천시"),
            Region("평택시")
        )
    ),
    Region(
        "경상남도",
        listOf(
            Region("거제시"),
            Region("거창군"),
            Region("김해시"),
            Region("남해군"),
            Region("산청군"),
            Region("창녕군"),
            Region("통영시"),
            Region("하동군"),
            Region("함안군"),
            Region("함양군"),
            Region("합천군")
        )
    ),
    Region(
        "경상북도",
        listOf(
            Region("경산시"),
            Region("문경시"),
            Region("봉화군"),
            Region("상주시"),
            Region("성주군"),
            Region("영천시"),
            Region("울진군"),
            Region("의성군"),
            Region("청송군"),
            Region("포항시")
        )
    ),
    Region(
        "광주광역시",
        listOf(
            Region("남구"),
            Region("서구")
        )
    ),
    Region(
        "대구광역시",
        listOf(
            Region("군위군"),
            Region("남구"),
            Region("달서구"),
            Region("달성군"),
            Region("서구")
        )
    ),
    Region(
        "대전광역시",
        listOf() // No subregions
    ),
    Region(
        "부산광역시",
        listOf(
            Region("북구"),
            Region("사하구"),
            Region("수영구")
        )
    ),
    Region(
        "서울특별시",
        listOf(
            Region("강남구"),
            Region("강동구"),
            Region("강서구"),
            Region("관악구"),
            Region("광진구"),
            Region("구로구"),
            Region("동작구"),
            Region("마포구"),
            Region("서대문구"),
            Region("서초구"),
            Region("용산구"),
            Region("은평구"),
            Region("중구")
        )
    ),
    Region(
        "세종특별자치시",
        listOf() // No subregions
    ),
    Region(
        "울산광역시",
        listOf(
            Region("남구")
        )
    ),
    Region(
        "인천광역시",
        listOf(
            Region("강화군"),
            Region("계양구"),
            Region("동구"),
            Region("서구"),
            Region("연수구")
        )
    ),
    Region(
        "전라남도",
        listOf(
            Region("강진군"),
            Region("곡성군"),
            Region("광양시"),
            Region("나주시"),
            Region("담양군"),
            Region("무안군"),
            Region("보성군"),
            Region("영광군"),
            Region("영암군"),
            Region("장성군"),
            Region("함평군"),
            Region("해남군"),
            Region("화순군")
        )
    ),
    Region(
        "전북특별자치도",
        listOf(
            Region("김제시"),
            Region("부안군"),
            Region("순창군"),
            Region("완주군"),
            Region("익산시"),
            Region("임실군"),
            Region("전주시")
        )
    ),
    Region(
        "제주특별자치도",
        listOf() // No subregions
    ),

    Region(
        "충청남도",
        listOf(
            Region("계룡시"),
            Region("공주시"),
            Region("금산군"),
            Region("논산시"),
            Region("당진시"),
            Region("보령시"),
            Region("부여군"),
            Region("서산시"),
            Region("아산시"),
            Region("예산군"),
            Region("천안시"),
            Region("청양군"),
            Region("태안군"),
            Region("홍성군")
        )
    ),
    Region(
        "충청북도",
        listOf(
            Region("보은군"),
            Region("영동군"),
            Region("옥천군"),
            Region("음성군"),
            Region("증평군"),
            Region("진천군"),
            Region("청주시"),
            Region("충주시")
        )
    )
)

@Composable
fun RegionSelectionScreen() {
    var selectedRegions by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "선호 지역",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        RegionGrid(
            regions = listOf(
                "서울", "경기", "인천", "부산",
                "경남", "경북", "대구", "울산",
                "대전", "충남", "충북", "광주",
                "전남", "전북", "강원", "제주",
                "세종"
            ),
            selectedRegions = selectedRegions,
            onRegionSelected = { region ->
                selectedRegions = if (region in selectedRegions) {
                    selectedRegions - region
                } else {
                    selectedRegions + region
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        SelectedRegionsSection(
            selectedRegions = selectedRegions,
            onRemoveRegion = { region ->
                selectedRegions = selectedRegions - region
            },
            onClearAll = {
                selectedRegions = emptySet()
            }
        )

        Button(
            onClick = { /* Apply selection */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E5CFF))
        ) {
            Text("적용", color = Color.White)
        }
    }
}

@Composable
fun RegionGrid(
    regions: List<String>,
    selectedRegions: Set<String>,
    onRegionSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(regions) { region ->
            RegionChip(
                region = region,
                isSelected = region in selectedRegions,
                onSelected = { onRegionSelected(region) }
            )
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
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) Color(0xFF5E5CFF) else Color.LightGray,
        onClick = onSelected
    ) {
        Text(
            text = region,
            color = if (isSelected) Color.White else Color.Black,
            modifier = Modifier.padding(8.dp),
            fontSize = 14.sp
        )
    }
}

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
        selectedRegions.forEach { region ->
            AssistChip(
                onClick = { onRemoveRegion(region) },
                label = { Text(region) },
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
}

@Preview(showBackground = true)
@Composable
fun Preview(){
    RegionSelectionScreen()
}