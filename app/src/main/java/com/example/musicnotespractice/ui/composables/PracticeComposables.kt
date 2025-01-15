package com.example.musicnotespractice.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PracticeComposables(
    modifier: Modifier,
    alankars: List<List<String>>,
    isAlankarPlaying: MutableState<Boolean>,
    currentAlankarIndex: MutableIntState,
    alankarCurrentIndex: MutableIntState,
    lazyListState: LazyListState,
    allSwars: List<String>,
    currPlayingSwar: MutableState<String>
){

    val currentPracticeComposable = remember{ mutableIntStateOf(0)}

    Row(
        modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.SkipPrevious,
            "Previous Practice",
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
                .align(Alignment.CenterVertically)
                .clickable {
                    if(currentPracticeComposable.intValue > 0){
                        currentPracticeComposable.intValue--
                    }
                },
            tint = Color.White,
        )
        when (currentPracticeComposable.intValue) {
            0 -> AlankarPractice(
                Modifier
                    .weight(8f)
                    .align(Alignment.CenterVertically),
                alankars,
                isAlankarPlaying,
                currentAlankarIndex,
                alankarCurrentIndex,
                lazyListState,
            )

            1 -> SwarPractice(
                Modifier
                    .weight(8f)
                    .align(Alignment.CenterVertically),
                allSwars,
                currPlayingSwar
            )
        }
        Icon(
            imageVector = Icons.Default.SkipNext,
            "Next Practice",
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
                .align(Alignment.CenterVertically)
                .clickable {
                    if(currentPracticeComposable.intValue < 1){
                        currentPracticeComposable.intValue++
                    }
                },
            tint = Color.White,
        )
    }
}

@Preview
@Composable
fun PracticeComposablesPreview(){
    val isAlankarPlaying = remember{ mutableStateOf(false) }
    val currentAlankarIndex = remember{ mutableIntStateOf(0)}
    val alankarCurrentIndex = remember{ mutableIntStateOf(0)}
    val lazyListState = rememberLazyListState()
    val currPlayingSwar = remember { mutableStateOf("Sa") }
    PracticeComposables(
        Modifier,
        listOf(
            listOf("Sa", "_", "_", "_", "Re", "_", "_", "_", "Ga", "_", "_", "_", "Ma", "_", "_", "_", "Pa", "_", "_", "_", "Dha", "_", "_", "_", "Ni", "_", "_", "_", "Sa","_", "_", "_", "Sa", "_", "_", "_", "Ni", "_", "_", "_", "Dha", "_", "_", "_", "Pa", "_", "_", "_", "Ma", "_", "_", "_", "Ga", "_", "_", "_", "Re", "_", "_", "_", "Sa", "_", "_", "_"),
            listOf("Sa", "_", "Sa", "_", "Re", "_", "Re", "_", "Ga", "_", "Ga", "_", "Ma", "_", "Ma", "_", "Pa", "_", "Pa", "_", "Dha", "_", "Dha", "_", "Ni", "_", "Ni", "_", "Sa", "_", "Sa", "_", "Sa", "_", "Sa", "_", "Ni", "_", "Ni", "_", "Dha", "_", "Dha", "_", "Pa", "_", "Pa", "_", "Ma", "_", "Ni", "_", "Ga", "_", "Ga", "_", "Re", "_", "Re", "_", "Sa", "_", "Sa", "_"),
            listOf("Sa", "Sa", "Sa", "_", "Re", "Re", "Re", "_", "Ga", "Ga", "Ga", "_", "Ma", "Ma", "Ma", "_", "Pa", "Pa", "Pa", "_", "Dha", "Dha", "Dha", "_", "Ni", "Ni", "Ni", "_", "Sa", "Sa", "Sa", "_", "Sa", "Sa", "Sa", "_", "Ni", "Ni", "Ni", "_", "Dha", "Dha", "Dha", "_", "Pa", "Pa", "Pa", "_", "Ma", "Ma", "Ma", "_", "Ga", "Ga", "Ga", "_", "Re", "Re", "Re", "_", "Sa", "Sa", "Sa", "_"),
            listOf("Sa", "Sa", "Sa", "Sa", "Re", "Re", "Re", "Re", "Ga", "Ga", "Ga", "Ga", "Ma", "Ma", "Ma", "Ma", "Pa", "Pa", "Pa", "Pa", "Dha", "Dha", "Dha", "Dha", "Ni", "Ni", "Ni", "Ni", "Sa", "Sa", "Sa", "Sa", "Sa", "Sa", "Sa", "Sa", "Ni", "Ni", "Ni", "Ni", "Dha", "Dha", "Dha", "Dha", "Pa", "Pa", "Pa", "Pa", "Ma", "Ma", "Ma", "Ma", "Ga", "Ga", "Ga", "Ga", "Re", "Re", "Re", "Re", "Sa", "Sa", "Sa", "Sa")
        ),
        isAlankarPlaying,
        currentAlankarIndex,
        alankarCurrentIndex,
        lazyListState,
        allSwars = listOf("Sa", "TSa", "Re", "TRe", "Ga", "Ma", "TMa", "Pa", "TPa", "Dha", "TDha", "Ni"),
        currPlayingSwar =  currPlayingSwar
    )
}