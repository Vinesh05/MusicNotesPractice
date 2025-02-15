package com.example.musicnotespractice.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicnotespractice.ui.theme.BackgroundColor
import com.example.musicnotespractice.ui.theme.ButtonColor
import com.example.musicnotespractice.ui.theme.MusicNotesPracticeTheme
import com.example.musicnotespractice.viewmodel.AudioBufferViewModel
import com.example.musicnotespractice.viewmodel.PitchViewModel
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.model.BarData
import com.himanshoe.charty.common.axis.AxisConfig
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.roundToInt

@Composable
fun PitchDetector(
    modifier: Modifier,
    pitchViewModel: PitchViewModel,
    audioBufferViewModel: AudioBufferViewModel,
    currPlayingSwar: MutableState<String>
) {

    val allSwars = arrayOf("Dha", "TDha", "Ni", "Sa", "TSa", "Re", "TRe", "Ga", "Ma", "TMa", "Pa", "TPa")

    val pitch by pitchViewModel.pitchStateFlow.collectAsState()
    val audioBuffer by audioBufferViewModel.audioBufferStateFlow.collectAsState()
    var swarIndex = (12 * log2(pitch / 27.5)) % 12
    val saptak = floor(log2(pitch / 27.5))

    val musicalFrequencies = remember {
        listOf(
            16.35 to 32.70,    // Octave 0: C0 to C1
            32.70 to 65.41,    // Octave 1: C1 to C2
            65.41 to 130.81,   // Octave 2: C2 to C3
            130.81 to 261.63,  // Octave 3: C3 to C4
            261.63 to 523.25,  // Octave 4: C4 to C5 (middle C)
            523.25 to 1046.50, // Octave 5: C5 to C6
            1046.50 to 2093.00,// Octave 6: C6 to C7
            2093.00 to 4186.01,// Octave 7: C7 to C8
            4186.01 to 8372.02,// Octave 8: C8 to C9
            8372.02 to 16744.04// Octave 9: C9 to C10
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        if(pitch>0 && swarIndex>0){
            if(swarIndex.roundToInt()==12){
                swarIndex = 0.0
            }
            currPlayingSwar.value = allSwars[swarIndex.roundToInt()]
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
                    .weight(1f),
                color = Color.White,
                textAlign = TextAlign.Center,
                text = "Pitch: ${pitch.roundToInt()} \n Octave: $saptak Note: ${currPlayingSwar.value}"
            )
        }
        else{
            currPlayingSwar.value = " "
            Text(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp),
                color = Color.White,
                textAlign = TextAlign.Center,
                text = "No Pitch Detected: $pitch"
            )
        }

        CircularPitchUI(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(4f),
            currPlayingSwar.value
        )

        val fftSize = audioBuffer.size
        val frequencyBins by remember(audioBuffer) {
            if (!audioBuffer[0].isNaN() && audioBuffer.size > 2) {
                val binAverages = musicalFrequencies.mapIndexed { octave, (lowFreq, highFreq) ->
                    val samplingRate = 44100f
                    val startBin = (lowFreq * fftSize / samplingRate).toInt().coerceIn(0, fftSize/2)
                    val endBin = (highFreq * fftSize / samplingRate).toInt().coerceIn(0, fftSize/2)
                    val binAverage = audioBuffer
                        .slice(startBin until endBin)
                        .map { it.absoluteValue }
                        .average()
                    binAverage
                }

                val totalAverage = binAverages.sum()

                mutableStateOf(
                    List(musicalFrequencies.size) { octave ->
                        val percentage = if (totalAverage > 0) {
                            (binAverages[octave] / totalAverage * 100).toFloat()
                        } else {
                            0f
                        }
                        BarData(
                            "C${octave}",
                            percentage
                        )
                    }
                )
            } else {
                mutableStateOf(
                    listOf(
                        BarData("C0", 100f),
                        BarData("C1", 80f),
                        BarData("C2", 60f),
                        BarData("C3", 40f),
                        BarData("C4", 20f),
                        BarData("C5", 20f),
                        BarData("C6", 40f),
                        BarData("C7", 60f),
                        BarData("C8", 80f),
                        BarData("C9", 100f),
                    )
                )
            }
        }

        BarChart(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(4f)
                .padding(16.dp)
                .fillMaxWidth(0.9f),
            barData = frequencyBins,
            color = ButtonColor,
            axisConfig = AxisConfig(
                showAxis = true,
                isAxisDashed = false,
                showUnitLabels = true,
                showXLabels = true,
                xAxisColor = Color.White,
                yAxisColor = Color.White,
                textColor = Color.White,
            ),
            onBarClick = {}
        )

    }
}

@Preview(showBackground = true)
@Composable
fun PitchDetectorPreview() {
    MusicNotesPracticeTheme {
        val currPlayingSwar = remember { mutableStateOf(" ")}
        PitchDetector(
            modifier = Modifier.background(BackgroundColor)
                .padding(16.dp),
            PitchViewModel(),
            AudioBufferViewModel(),
            currPlayingSwar
        )
    }
}