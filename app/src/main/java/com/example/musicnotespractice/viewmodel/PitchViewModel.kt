package com.example.musicnotespractice.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PitchViewModel {
    private val _pitchStateFlow = MutableStateFlow(0f)
    val pitchStateFlow: StateFlow<Float> = _pitchStateFlow.asStateFlow()

    fun updatePitch(pitch: Float) {
        _pitchStateFlow.value = pitch
    }

}