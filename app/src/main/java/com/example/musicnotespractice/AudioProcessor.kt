package com.example.musicnotespractice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class AudioProcessor(
    private val context: Context
) {

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var recorder: AudioRecord? = null
    private var bufferSize = 0
    private var yin = Yin(44100f)

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    fun startRecording() = CoroutineScope(Dispatchers.Default).launch {
        Log.d("AudioProcessor", "Starting recording...")
        if(!checkPermission()){
            Log.d("AudioProcessor", "No Permission")
            return@launch
        }
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)

        recorder?.startRecording()

        while (recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            val buffer = ShortArray(bufferSize)
            val numRead = recorder?.read(buffer, 0, bufferSize) ?: 0

            if (numRead > 0) {
                processAudioData(buffer, numRead)
            }
        }
    }

    private fun processAudioData(buffer: ShortArray, numRead: Int) {
//        Log.d("AudioProcessor", "Processing audio data...")
        val pitch = yin.getPitch(buffer)
        if(pitch>0) {
            Log.d("AudioProcessor", "Pitch: $pitch")
        }
        // Implement your audio processing logic here
        // For example, you can:
        // - Apply filters (e.g., low-pass, high-pass, band-pass)
        // - Perform noise reduction
        // - Detect specific audio events (e.g., voice activity detection)
        // - Extract features for machine learning models (e.g., MFCCs, Mel-spectrograms)
    }

    fun stopRecording() {
        Log.d("AudioProcessor", "Stopping recording...")
        recorder?.stop()
        recorder?.release()
        recorder = null
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 100
    }

}