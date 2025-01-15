package com.example.musicnotespractice.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.sin

class PitchCalibrator(
    context: Context
) {

    private var currentAudioTrack: AudioTrack? = null
    private val sampleRate = 44100
    private val sharedPrefs = context.getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
    private var calibrationOffset: Float = sharedPrefs.getFloat(Constants.SHARED_PREFERENCE_CALIBRATION_OFFSET_KEY, 0f)

    private fun generateCalibrationTone(frequency: Float, durationSeconds: Float): ByteArray {
        val numSamples = (sampleRate * durationSeconds).toInt()
        val sample = ByteArray(2 * numSamples)

        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate.toDouble()
            val value = (Short.MAX_VALUE * sin(2.0 * PI * frequency * time)).toInt().toShort()
            sample[2 * i] = (value.toInt() and 0xFF).toByte()
            sample[2 * i + 1] = (value.toInt() shr 8).toByte()
        }

        return sample
    }

    suspend fun playCalibrationTone(
        frequency: Float,
        durationSeconds: Float,
        pitchStateFlow: StateFlow<Float>,
        isCalibrating: MutableState<Boolean>
    ) = suspendCancellableCoroutine { continuation ->

        calibrationOffset = 1f

        val audioData = generateCalibrationTone(frequency, durationSeconds)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = maxOf(minBufferSize, audioData.size)

        try {
            currentAudioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build().apply {
                    // Set up completion listener
                    setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                        override fun onPeriodicNotification(track: AudioTrack) {}

                        override fun onMarkerReached(track: AudioTrack) {
                            val pitch = pitchStateFlow.value
                            Log.d("Pitch Calibrator", "Pitch after Calibrating: $pitch")
                            calibrate(frequency, pitch)
                            isCalibrating.value = false

                            continuation.resume(Unit)
                            release()
                            currentAudioTrack = null
                        }
                    })

                    // Write audio data
                    write(audioData, 0, audioData.size)

                    // Set marker position at the end of the audio
                    notificationMarkerPosition = audioData.size / 2 // Divide by 2 because we're using 16-bit samples

                    play()
                }

            continuation.invokeOnCancellation {
                currentAudioTrack?.apply {
                    stop()
                    release()
                }
                currentAudioTrack = null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            currentAudioTrack?.release()
            currentAudioTrack = null
            continuation.resume(Unit)
        }

    }

    fun calibrate(referenceFrequency: Float, detectedFrequency: Float) {
        val diff = referenceFrequency - detectedFrequency
        calibrationOffset = diff / referenceFrequency
        Log.d("PitchCalibrator", "Calibration offset: $calibrationOffset")
        sharedPrefs.edit().putFloat(Constants.SHARED_PREFERENCE_CALIBRATION_OFFSET_KEY, calibrationOffset).apply()
    }

    fun getCalibratedPitch(frequency: Float): Float {
        return frequency * calibrationOffset
    }

    fun setCalibrationOffset(offset: Float){
        calibrationOffset = offset
    }

    companion object {
        const val REFERENCE_A4 = 440f // A4 note
        const val CALIBRATION_DURATION = 2f // seconds
    }
}