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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
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

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d("MainActivity", "Permission Granted")
                }
                else{
                    Log.d("MainActivity", "Permission Denied")
                }
            }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

    }


}

@Composable
fun PitchDetector(modifier: Modifier) {

    val allNotes = arrayOf("Dha", "TDha", "Ni", "Sa", "TSa", "Re", "TRe", "Ga", "Ma", "TMa", "Pa", "TPa")

    val context = LocalContext.current
    val pitchViewModel = remember { PitchViewModel() }
    val audioRecorder = remember{ AudioProcessor(context, pitchViewModel) }
    val isRecording = remember { mutableStateOf(false) }
    val pitch by pitchViewModel.pitchStateFlow.collectAsState()
    var note = (12 * log2(pitch / 440.0)) % 12
    val octave = floor(log2(pitch / 440.0)) + 4
    Column(
        modifier = modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
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

        var noteName: String
        if(pitch>0 && note>0){
            if(note.roundToInt()==12){
                note = 0.0
            }
            noteName = allNotes[note.roundToInt()]
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
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
                textAlign = TextAlign.Center,
                text = "No Pitch Detected: $pitch"
            )
        }

        CircularPitchUI(
            modifier = Modifier.weight(8f),
            noteName
        )

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MusicNotesPracticeTheme {
        PitchDetector(
            modifier = Modifier.padding(16.dp)
        )
    }
}