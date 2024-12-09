package com.example.musicnotespractice

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicnotespractice.ui.theme.MusicNotesPracticeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicNotesPracticeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }



    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val audioRecorder = remember{ AudioProcessor(context) }
    val isRecording = remember { mutableStateOf(false) }
    Button(
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
        },
        modifier = modifier
    ) {
        Text(
            text = "Start/Stop Recording",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MusicNotesPracticeTheme {
        Greeting("Android")
    }
}