package com.example.musicnotespractice

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.musicnotespractice.ui.composables.MainButtons
import com.example.musicnotespractice.ui.composables.PitchDetector
import com.example.musicnotespractice.ui.composables.PracticeComposables
import com.example.musicnotespractice.ui.theme.BackgroundColor
import com.example.musicnotespractice.ui.theme.MusicNotesPracticeTheme
import com.example.musicnotespractice.utils.AudioProcessor
import com.example.musicnotespractice.utils.PitchCalibrator
import com.example.musicnotespractice.viewmodel.AudioBufferViewModel
import com.example.musicnotespractice.viewmodel.PitchViewModel
import kotlinx.coroutines.delay

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
    val currPlayingSwar = remember { mutableStateOf(" ") }
    val audioBufferViewModel = remember { AudioBufferViewModel() }
    val pitchCalibrator = remember { PitchCalibrator(context) }
    val audioRecorder = remember{ AudioProcessor(context, pitchViewModel, audioBufferViewModel, pitchCalibrator) }
    val isRecording = remember { mutableStateOf(false) }
    val isTicking = remember { mutableStateOf(false) }
    val tickPlayer = remember {
        MediaPlayer.create(context, R.raw.tick_sound).apply {
            isLooping = false
        }
    }
    val alankars = listOf(
        listOf("Sa", "_", "_", "_", "Re", "_", "_", "_", "Ga", "_", "_", "_", "Ma", "_", "_", "_", "Pa", "_", "_", "_", "Dha", "_", "_", "_", "Ni", "_", "_", "_", "Sa","_", "_", "_", "Sa", "_", "_", "_", "Ni", "_", "_", "_", "Dha", "_", "_", "_", "Pa", "_", "_", "_", "Ma", "_", "_", "_", "Ga", "_", "_", "_", "Re", "_", "_", "_", "Sa", "_", "_", "_"),
        listOf("Sa", "_", "Sa", "_", "Re", "_", "Re", "_", "Ga", "_", "Ga", "_", "Ma", "_", "Ma", "_", "Pa", "_", "Pa", "_", "Dha", "_", "Dha", "_", "Ni", "_", "Ni", "_", "Sa", "_", "Sa", "_", "Sa", "_", "Sa", "_", "Ni", "_", "Ni", "_", "Dha", "_", "Dha", "_", "Pa", "_", "Pa", "_", "Ma", "_", "Ni", "_", "Ga", "_", "Ga", "_", "Re", "_", "Re", "_", "Sa", "_", "Sa", "_"),
        listOf("Sa", "Sa", "Sa", "_", "Re", "Re", "Re", "_", "Ga", "Ga", "Ga", "_", "Ma", "Ma", "Ma", "_", "Pa", "Pa", "Pa", "_", "Dha", "Dha", "Dha", "_", "Ni", "Ni", "Ni", "_", "Sa", "Sa", "Sa", "_", "Sa", "Sa", "Sa", "_", "Ni", "Ni", "Ni", "_", "Dha", "Dha", "Dha", "_", "Pa", "Pa", "Pa", "_", "Ma", "Ma", "Ma", "_", "Ga", "Ga", "Ga", "_", "Re", "Re", "Re", "_", "Sa", "Sa", "Sa", "_"),
        listOf("Sa", "Sa", "Sa", "Sa", "Re", "Re", "Re", "Re", "Ga", "Ga", "Ga", "Ga", "Ma", "Ma", "Ma", "Ma", "Pa", "Pa", "Pa", "Pa", "Dha", "Dha", "Dha", "Dha", "Ni", "Ni", "Ni", "Ni", "Sa", "Sa", "Sa", "Sa", "Sa", "Sa", "Sa", "Sa", "Ni", "Ni", "Ni", "Ni", "Dha", "Dha", "Dha", "Dha", "Pa", "Pa", "Pa", "Pa", "Ma", "Ma", "Ma", "Ma", "Ga", "Ga", "Ga", "Ga", "Re", "Re", "Re", "Re", "Sa", "Sa", "Sa", "Sa")
    )
    val lazyListState = rememberLazyListState()
    val currentAlankarIndex = remember { mutableIntStateOf(0) }
    val alankarCurrentIndex = remember { mutableIntStateOf(0) }
    val isAlankarPlaying = remember { mutableStateOf(false) }
    val allSwars = listOf("Sa", "TSa", "Re", "TRe", "Ga", "Ma", "TMa", "Pa", "TPa", "Dha", "TDha", "Ni")

    LaunchedEffect(isAlankarPlaying, alankarCurrentIndex, isTicking) {
        while(true) {
            delay(1000)
            if (isAlankarPlaying.value) {
                alankarCurrentIndex.intValue =
                    (alankarCurrentIndex.intValue + 1) % alankars[currentAlankarIndex.intValue].size
                Log.d(
                    "AlankarPractice",
                    "Inside Launched Effect Current Index: ${alankarCurrentIndex.intValue}"
                )
                lazyListState.animateScrollToItem(alankarCurrentIndex.intValue)
            }
            if (isTicking.value) {
                tickPlayer.seekTo(0)
                tickPlayer.start()
            }
        }
    }

    Column(
        modifier = modifier
    ){
        MainButtons(
            Modifier.weight(0.5f),
            context,
            audioRecorder,
            pitchCalibrator,
            pitchViewModel,
            isRecording,
            isTicking
        )
        PracticeComposables(
            Modifier.weight(3f),
            alankars,
            isAlankarPlaying,
            currentAlankarIndex,
            alankarCurrentIndex,
            lazyListState,
            allSwars,
            currPlayingSwar
        )
        PitchDetector(
            Modifier.weight(8.5f),
            pitchViewModel,
            audioBufferViewModel,
            currPlayingSwar
        )
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
