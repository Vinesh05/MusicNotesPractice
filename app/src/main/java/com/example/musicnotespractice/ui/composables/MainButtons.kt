package com.example.musicnotespractice.ui.composables

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.musicnotespractice.utils.AudioProcessor
import com.example.musicnotespractice.utils.PitchCalibrator
import com.example.musicnotespractice.viewmodel.PitchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun MainButtons(
    modifier: Modifier = Modifier,
    context: Context,
    audioRecorder: AudioProcessor,
    pitchCalibrator: PitchCalibrator,
    pitchViewModel: PitchViewModel,
    isRecording: MutableState<Boolean>,
    isTicking: MutableState<Boolean>
) {
    val isCalibrating = remember { mutableStateOf(false) }
    val showCalibrationDialog = remember { mutableStateOf(false) }
    val calibrationOffset = remember { mutableStateOf("0") }

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

    val calibrationInteractionSource = remember { MutableInteractionSource() }
    val viewConfiguration = LocalViewConfiguration.current

    if (showCalibrationDialog.value) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog.value = false },
            title = { Text("Calibration Offset") },
            text = {
                OutlinedTextField(
                    value = calibrationOffset.value,
                    onValueChange = { newValue ->
                        // Only allow numeric input with optional decimal point and negative sign
                        if (newValue.isEmpty() || newValue.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                            calibrationOffset.value = newValue
                        }
                    },
                    label = { Text("Enter offset in cents") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        calibrationOffset.value.toFloatOrNull()?.let { offset ->
                            pitchCalibrator.setCalibrationOffset(offset)
                            Toast.makeText(context, "Calibration offset set to $offset cents", Toast.LENGTH_SHORT).show()
                        }
                        showCalibrationDialog.value = false
                    }
                ) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCalibrationDialog.value = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(calibrationInteractionSource) {
        var isLongClick = false

        calibrationInteractionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isLongClick = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    isLongClick = true
                    showCalibrationDialog.value = true
                }

                is PressInteraction.Release -> {
                    if (isLongClick.not()) {
                        if(!isRecording.value){
                            Toast.makeText(context, "Please start recording first", Toast.LENGTH_SHORT).show()
                            return@collectLatest
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
                }
            }
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            modifier = if(isTicking.value){
                buttonModifierOn.align(Alignment.CenterVertically)
            } else {
                buttonModifierOff.align(Alignment.CenterVertically)
            },
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
        val calibrationButtonModifier = if(isCalibrating.value) {
            buttonModifierOn.align(Alignment.CenterVertically)
        } else {
            buttonModifierOff.align(Alignment.CenterVertically)
        }
        Button(
            modifier = calibrationButtonModifier,
            contentPadding = PaddingValues(8.dp),
            colors = if(isCalibrating.value) buttonColorsOn else buttonColorsOff,
            onClick = {},
            interactionSource = calibrationInteractionSource
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
