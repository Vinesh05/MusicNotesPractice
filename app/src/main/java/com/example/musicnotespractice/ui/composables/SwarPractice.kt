package com.example.musicnotespractice.ui.composables

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
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
    val swarAverageResponseTime = remember { allSwars.associateWith{0.0}.toMutableMap() }
    val currSwarStartTime = remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit){
        while(true){
            delay(500)
            if(currPlayingSwar.value==currSelectedSwar.value) {
                val responseTime = System.currentTimeMillis() - currSwarStartTime.longValue
                swarAverageResponseTime[currSelectedSwar.value] =
                    (swarAverageResponseTime[currSelectedSwar.value]!! + responseTime) / 2

                currSelectedSwar.value = allSwars.random()
                currSwarStartTime.longValue = System.currentTimeMillis()
            }
        }
    }

    Row(
        modifier = modifier
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(2f)
        ) {

            TextBox(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                swar = currSelectedSwar.value,
                fontSize = 32.sp,
                isHighlighted = false
            )

            Button(
                onClick = {
                    isSwarPlaying.value = !isSwarPlaying.value
                    currSwarStartTime.longValue = System.currentTimeMillis()
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp),
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
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
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

        ResponseTimeScores(
            Modifier
                .weight(3f)
                .fillMaxWidth(),
            swarAverageResponseTime
        )
    }
}

@Composable
fun ResponseTimeScores(
    modifier: Modifier,
    swarAverageResponseTime: MutableMap<String, Double>
) {
    Log.d("SwarPractice", "Response Time Scores: ${swarAverageResponseTime.values.size}")
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center
    ) {
        items(swarAverageResponseTime.toList()) { (key, value) ->
            Text(
                text = "$key: $value ms",
                color = TextColor,
                modifier = Modifier.padding(0.dp)
            )
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
