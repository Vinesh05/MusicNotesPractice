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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.example.musicnotespractice.viewmodel.AudioBufferViewModel
import com.example.musicnotespractice.viewmodel.PitchViewModel

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
    val audioRecorder = remember{ AudioProcessor(context, pitchViewModel, audioBufferViewModel) }
    val isRecording = remember { mutableStateOf(false) }
    val isTicking = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ){
        MainButtons(audioRecorder, isRecording, isTicking)
        PitchDetector(
            modifier = modifier,
            pitchViewModel,
            audioBufferViewModel
        )
    }
}

@Composable
fun MainButtons(
    audioRecorder: AudioProcessor,
    isRecording: MutableState<Boolean>,
    isTicking: MutableState<Boolean>
) {

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
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MusicNotesPracticeTheme {
        MainScreen(
            modifier = Modifier.background(BackgroundColor).padding(16.dp)
        )
    }
}
