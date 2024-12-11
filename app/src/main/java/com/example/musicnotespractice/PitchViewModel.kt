package com.example.musicnotespractice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PitchViewModel {
    private val _pitchStateFlow = MutableStateFlow(0f)
    val pitchStateFlow: StateFlow<Float> = _pitchStateFlow

    fun updatePitch(pitch: Float) {
        _pitchStateFlow.value = pitch
    }

}