package com.example.musicnotespractice.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.musicnotespractice.viewmodel.AudioBufferViewModel
import com.example.musicnotespractice.viewmodel.PitchViewModel
import kotlinx.coroutines.*
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

class AudioProcessor(
    private val context: Context,
    private val pitchViewModel: PitchViewModel,
    private val audioBufferViewModel: AudioBufferViewModel
) {

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var recorder: AudioRecord? = null
    private var bufferSize = 0
    private val FFT = FastFourierTransformer(DftNormalization.UNITARY)
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

        val pitch = yin.getPitch(buffer)
        pitchViewModel.updatePitch(pitch)

        val doubleArrayBuffer = buffer.map {
            it.toDouble()/Short.MAX_VALUE
        }.toDoubleArray()

        Log.d("AudioProcessor", "Num read: $numRead, buffer size: ${buffer.size} doubleArray: ${doubleArrayBuffer.contentToString()}")

        val dataArrayLength = doubleArrayBuffer.size
        val nextPowerOfTwo = 2.0.pow(ceil(ln(dataArrayLength.toDouble()) / ln(2.0))).toInt()
        val paddedArray = DoubleArray(nextPowerOfTwo)
        paddedArray.fill(0.0)
        System.arraycopy(doubleArrayBuffer, 0, paddedArray, 0, doubleArrayBuffer.size)

        val fftResult = FFT.transform(paddedArray, TransformType.FORWARD)
        val magnitudeSpectrum = fftResult.map { round(sqrt(it.real * it.real + it.imaginary * it.imaginary)*1000.0)/1000.0 }

        audioBufferViewModel.updateAudioBuffer(magnitudeSpectrum)

        if(pitch>0) {
            Log.d("AudioProcessor", "Pitch: $pitch")
        }
    }

    fun stopRecording() {
        Log.d("AudioProcessor", "Stopping recording...")
        recorder?.stop()
        recorder?.release()
        recorder = null
    }

}