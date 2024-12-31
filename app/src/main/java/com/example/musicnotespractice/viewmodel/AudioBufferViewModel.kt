package com.example.musicnotespractice.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AudioBufferViewModel {
    private val _audioBufferStateFlow = MutableStateFlow(listOf(0.0))
    val audioBufferStateFlow: StateFlow<List<Double>> = _audioBufferStateFlow

    fun updateAudioBuffer(buffer: List<Double>) {
        _audioBufferStateFlow.value = buffer
    }
}