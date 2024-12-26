package com.example.musicnotespractice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.musicnotespractice.ui.composables.CircularPitchUI
import com.example.musicnotespractice.ui.theme.BackgroundColor
import com.example.musicnotespractice.ui.theme.MusicNotesPracticeTheme
import com.example.musicnotespractice.viewmodel.AudioBufferViewModel
import com.example.musicnotespractice.viewmodel.PitchViewModel
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.model.BarData
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicNotesPracticeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                        .background(BackgroundColor),
                    ) { innerPadding ->
                    PitchDetector(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d("MainActivity", "Permission Granted")
                }
                else{
                    Log.d("MainActivity", "Permission Denied")
                }
            }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

    }


}

@Composable
fun PitchDetector(modifier: Modifier) {

    val allNotes = arrayOf("Dha", "TDha", "Ni", "Sa", "TSa", "Re", "TRe", "Ga", "Ma", "TMa", "Pa", "TPa")

    val context = LocalContext.current
    val pitchViewModel = remember { PitchViewModel() }
    val audioBufferViewModel = remember { AudioBufferViewModel() }
    val audioRecorder = remember{ AudioProcessor(context, pitchViewModel, audioBufferViewModel) }
    val isRecording = remember { mutableStateOf(false) }
    val pitch by pitchViewModel.pitchStateFlow.collectAsState()
    val audioBuffer by audioBufferViewModel.audioBufferStateFlow.collectAsState()
    var note = (12 * log2(pitch / 440.0)) % 12
    val octave = floor(log2(pitch / 440.0)) + 4

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
        Button(
            modifier = Modifier.weight(1f),
            onClick = {
                if(!isRecording.value) {
                    Log.d("MainActivity", "Starting recording...")
                    audioRecorder.startRecording()
                    isRecording.value = true
                }
                else {
                    Log.d("MainActivity", "Stopping recording...")
                    audioRecorder.stopRecording()
                    isRecording.value = false
                }
            }
        ) {
            Text(
                text = if(isRecording.value) "Stop Recording" else "Start Recording",
            )
        }

        val noteName: String
        if(pitch>0 && note>0){
            if(note.roundToInt()==12){
                note = 0.0
            }
            noteName = allNotes[note.roundToInt()]
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
                color = Color.White,
                textAlign = TextAlign.Center,
                text = "Pitch: ${pitch.roundToInt()} \n Octave: $octave Note: $noteName"
            )
        }
        else{
            noteName = " "
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                color = Color.White,
                textAlign = TextAlign.Center,
                text = "No Pitch Detected: $pitch"
            )
        }

        CircularPitchUI(
            modifier = Modifier.weight(4f),
            noteName
        )

        val fftSize = audioBuffer.size

        val frequencyBins by remember(audioBuffer) {
            if (!audioBuffer[0].isNaN() && audioBuffer.size > 2) {
                mutableStateOf(
                    musicalFrequencies.mapIndexed { octave, (lowFreq, highFreq) ->
                        val samplingRate = 44100f

                        val startBin = (lowFreq * fftSize / samplingRate).toInt().coerceIn(0, fftSize/2)
                        val endBin = (highFreq * fftSize / samplingRate).toInt().coerceIn(0, fftSize/2)

                        val binAverage = audioBuffer
                            .slice(startBin until endBin)
                            .map { it.absoluteValue }
                            .average()

                        BarData(
                            "C${octave}",
                            binAverage.toFloat() * 100f
                        )
                    }
                )
            } else {
                mutableStateOf(
                    listOf(
                        BarData(1, 100f),
                    )
                )
            }
        }

        BarChart(
            modifier = Modifier
                .weight(4f)
                .fillMaxWidth()
                .padding(16.dp),
            barData = frequencyBins,
            color = Color.Blue,
            onBarClick = {}
        )

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MusicNotesPracticeTheme {
        PitchDetector(
            modifier = Modifier.background(BackgroundColor)
                .padding(16.dp)
        )
    }
}