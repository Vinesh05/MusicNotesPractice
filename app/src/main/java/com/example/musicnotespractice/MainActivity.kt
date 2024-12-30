package com.example.musicnotespractice

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.musicnotespractice.ui.composables.PitchDetector
import com.example.musicnotespractice.ui.theme.BackgroundColor
import com.example.musicnotespractice.ui.theme.MusicNotesPracticeTheme
import com.example.musicnotespractice.utils.AudioProcessor
import com.example.musicnotespractice.utils.Constants
import com.example.musicnotespractice.utils.PitchCalibrator
import com.example.musicnotespractice.viewmodel.AudioBufferViewModel
import com.example.musicnotespractice.viewmodel.PitchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
                    MainScreen(
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
fun MainScreen(
    modifier: Modifier
){
    val context = LocalContext.current
    val pitchViewModel = remember { PitchViewModel() }
    val audioBufferViewModel = remember { AudioBufferViewModel() }
    val pitchCalibrator = remember { PitchCalibrator(context) }
    val audioRecorder = remember{ AudioProcessor(context, pitchViewModel, audioBufferViewModel, pitchCalibrator) }
    val isRecording = remember { mutableStateOf(false) }
    val isTicking = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ){
        MainButtons(context, audioRecorder, pitchCalibrator, pitchViewModel, isRecording, isTicking)
        PitchDetector(
            modifier = modifier,
            pitchViewModel,
            audioBufferViewModel
        )
    }
}

@Composable
fun MainButtons(
    context: Context,
    audioRecorder: AudioProcessor,
    pitchCalibrator: PitchCalibrator,
    pitchViewModel: PitchViewModel,
    isRecording: MutableState<Boolean>,
    isTicking: MutableState<Boolean>
) {

    val isCalibrating = remember { mutableStateOf(false)}
    val buttonModifierOn = Modifier
            .shadow(
                elevation = 2.dp,
                shape = CircleShape,
                spotColor = Color(0xFF82C95A)
            )
            .defaultMinSize(4.dp, 4.dp)
    val buttonModifierOff = Modifier
            .shadow(
                elevation = 2.dp,
                shape = CircleShape,
                spotColor = Color(0xFFE1795A)
            )
            .defaultMinSize(4.dp, 4.dp)

    val buttonColorsOn = ButtonColors(
            containerColor = Color(0xFF82C95A),
            contentColor = Color.DarkGray,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.DarkGray
        )
    val buttonColorsOff = ButtonColors(
            containerColor = Color(0xFFE1795A),
            contentColor = Color.DarkGray,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.DarkGray
        )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            modifier = if(isTicking.value) buttonModifierOn else buttonModifierOff,
            contentPadding = PaddingValues(8.dp),
            colors = if(isTicking.value) buttonColorsOn else buttonColorsOff,
            onClick = {
                isTicking.value = !isTicking.value
            }
        ){
            Icon(
                imageVector = if(isRecording.value) Icons.Default.Timer else Icons.Default.TimerOff,
                "Start/Stop Ticker"
            )
        }
        TickerSound(isTicking)
        Button(
            modifier = if(isCalibrating.value) buttonModifierOn else buttonModifierOff,
            contentPadding = PaddingValues(8.dp),
            colors = if(isCalibrating.value) buttonColorsOn else buttonColorsOff,
            onClick = onClick@{
                if(!isRecording.value){
                    Toast.makeText(context, "Please start recording first", Toast.LENGTH_SHORT).show()
                    return@onClick
                }
                if(!isCalibrating.value){
                    isCalibrating.value = true
                    Log.d("MainActivity", "Starting calibration...")
                    val referenceFrequency = PitchCalibrator.REFERENCE_A4

                    CoroutineScope(Dispatchers.IO).launch {
                        pitchCalibrator.playCalibrationTone(
                            frequency = referenceFrequency,
                            durationSeconds = PitchCalibrator.CALIBRATION_DURATION,
                            pitchViewModel.pitchStateFlow,
                            isCalibrating
                        )

                    }
                }
            }
        ){
            Icon(
                imageVector = Icons.Default.AutoGraph,
                "Start Calibration"
            )
        }
        Button(
            modifier = if(isRecording.value) buttonModifierOn else buttonModifierOff,
            contentPadding = PaddingValues(8.dp),
            colors = if(isRecording.value) buttonColorsOn else buttonColorsOff,
            onClick = {
                if (!isRecording.value) {
                    Log.d("MainActivity", "Starting recording...")
                    audioRecorder.startRecording()
                    isRecording.value = true
                } else {
                    Log.d("MainActivity", "Stopping recording...")
                    audioRecorder.stopRecording()
                    isRecording.value = false
                }
            }
        ){
            Icon(
                imageVector = if(isRecording.value) Icons.Default.Mic else Icons.Default.MicOff,
                "Start/Stop Record"
            )
        }
    }
}

@Composable
fun TickerSound(
    isTicking: MutableState<Boolean>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tickJob = remember { mutableStateOf<Job?>(null) }
    val tickPlayer = remember {
        MediaPlayer.create(context, R.raw.tick_sound).apply {
            isLooping = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tickPlayer.release()
            tickJob.value?.cancel()
        }
    }

    LaunchedEffect(isTicking.value) {
        if (isTicking.value) {
            tickJob.value?.cancel()

            tickJob.value = scope.launch {
                while (isActive) {
                    Log.d("Metronome", "Loud Tick")
                    tickPlayer.seekTo(0)
                    tickPlayer.start()
                    delay(1000)
                }
            }
        }
        else {
            tickJob.value?.cancel()
            tickJob.value = null
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MusicNotesPracticeTheme {
        MainScreen(
            modifier = Modifier.background(BackgroundColor).padding(16.dp)
        )
    }
}
