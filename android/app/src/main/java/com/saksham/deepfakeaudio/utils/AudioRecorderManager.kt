package com.saksham.deepfakeaudio.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorderManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    var outputFile: File? = null
        private set

    fun start(): File {
        val file = File(context.cacheDir, "recorded_audio_${System.currentTimeMillis()}.m4a")
        outputFile = file
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16000)
            setAudioEncodingBitRate(128000)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        return file
    }

    fun amplitude(): Int = try { recorder?.maxAmplitude ?: 0 } catch (_: Exception) { 0 }

    fun stop(): File? {
        try { recorder?.stop() } catch (_: Exception) {}
        recorder?.release()
        recorder = null
        return outputFile
    }
}
