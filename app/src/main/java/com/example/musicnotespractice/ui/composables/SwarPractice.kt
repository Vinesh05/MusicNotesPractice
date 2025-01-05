package com.example.musicnotespractice.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicnotespractice.ui.theme.ButtonColor
import com.example.musicnotespractice.ui.theme.MusicNotesPracticeTheme
import com.example.musicnotespractice.ui.theme.TextColor
import kotlinx.coroutines.delay

@Composable
fun SwarPractice(
    modifier: Modifier = Modifier,
    allSwars: List<String>,
    currPlayingSwar: MutableState<String>
) {
    val isSwarPlaying = remember { mutableStateOf(false) }
    val currSelectedSwar = remember { mutableStateOf("Sa") }
    val swarAverageResponseTime = remember { HashMap<String, Double>()}

    LaunchedEffect(Unit){
        allSwars.forEach { swar->
            swarAverageResponseTime[swar] = 0.0
        }
        while(true){
            delay(500)
            if(currPlayingSwar.value==currSelectedSwar.value) {
                currSelectedSwar.value = allSwars.random()
            }
        }
    }

    Row(
        modifier = modifier
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextBox(
            Modifier.weight(2f),
            swar = currSelectedSwar.value,
            fontSize = 32.sp,
            isHighlighted = false
        )

        Column(
            modifier = Modifier.weight(3f),
        ) {
            ControlButtons(
                Modifier.fillMaxWidth().weight(1f),
                allSwars,
                isSwarPlaying,
                swarAverageResponseTime
            )
            ResponseTimeScores(
                Modifier.fillMaxWidth().weight(4f),
                swarAverageResponseTime
            )
        }
    }
}

@Composable
fun ResponseTimeScores(
    modifier: Modifier,
    swarAverageResponseTime: HashMap<String, Double>
) {
    Column(
        modifier = modifier
    ) {
        swarAverageResponseTime.forEach { (swar, averageResponseTime) ->
            Text(
                text = "$swar: $averageResponseTime ms"
            )
        }
    }
}

@Composable
fun ControlButtons(
    modifier: Modifier,
    allSwars: List<String>,
    isSwarPlaying: MutableState<Boolean>,
    swarAverageResponseTime: HashMap<String, Double>
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { isSwarPlaying.value = !isSwarPlaying.value },
            modifier = Modifier.padding(8.dp),
            colors = ButtonColors(
                containerColor = ButtonColor,
                contentColor = TextColor,
                disabledContainerColor = Color.DarkGray,
                disabledContentColor = Color.LightGray
            )
        ) {
            Text(if (isSwarPlaying.value) "Pause" else "Play")
        }

        Button(
            onClick = {
                isSwarPlaying.value = false
                allSwars.forEach { swar ->
                    swarAverageResponseTime[swar] = 0.0
                }
            },
            modifier = Modifier.padding(8.dp),
            colors = ButtonColors(
                containerColor = ButtonColor,
                contentColor = TextColor,
                disabledContainerColor = Color.DarkGray,
                disabledContentColor = Color.LightGray
            )
        ) {
            Text("Reset")
        }
    }
}

@Preview
@Composable
fun SwarPracticePreview(){
    MusicNotesPracticeTheme {
        val currPlayingSwar = remember { mutableStateOf("Sa") }
        SwarPractice(
            allSwars = listOf("Sa", "TSa", "Re", "TRe", "Ga", "Ma", "TMa", "Pa", "TPa", "Dha", "TDha", "Ni"),
            currPlayingSwar =  currPlayingSwar
        )
    }
}
