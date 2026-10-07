package com.saksham.deepfakeaudio

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.saksham.deepfakeaudio.databinding.ActivityMainBinding
import com.saksham.deepfakeaudio.utils.AudioRecorderManager
import com.saksham.deepfakeaudio.viewmodel.MainViewModel
import com.saksham.deepfakeaudio.viewmodel.UiState
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var recorder: AudioRecorderManager
    private var selectedFile: File? = null
    private var isRecording = false

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) toggleRecording() else binding.recordingStatus.text = "Microphone permission denied"
    }

    private val pickAudio = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedFile = copyUriToCache(it)
            binding.recordingStatus.text = "Selected: ${selectedFile?.name}"
            animateTap(binding.btnUpload)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recorder = AudioRecorderManager(this)
        startEntranceAnimations()
        startIdleWave()

        binding.btnRecord.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                toggleRecording()
            } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        binding.btnUpload.setOnClickListener { pickAudio.launch("audio/*") }
        binding.btnAnalyze.setOnClickListener {
            selectedFile?.let { viewModel.analyze(it); animateTap(binding.btnAnalyze) }
                ?: run { binding.recordingStatus.text = "Record or upload audio first" }
        }
        observeState()
    }

    private fun toggleRecording() {
        if (!isRecording) {
            selectedFile = recorder.start()
            isRecording = true
            binding.btnRecord.text = "Stop Recording"
            binding.recordingStatus.text = "Recording... live waveform active"
            captureAmplitudes()
        } else {
            selectedFile = recorder.stop()
            isRecording = false
            binding.btnRecord.text = "Start Recording"
            binding.recordingStatus.text = "Recording saved. Tap Analyze Audio."
        }
        animateTap(binding.btnRecord)
    }

    private fun captureAmplitudes() {
        binding.waveformView.postDelayed(object : Runnable {
            override fun run() {
                if (isRecording) {
                    binding.waveformView.addAmplitude(recorder.amplitude())
                    binding.waveformView.postDelayed(this, 70)
                }
            }
        }, 70)
    }

    private fun startIdleWave() {
        binding.waveformView.postDelayed(object : Runnable {
            override fun run() {
                if (!isRecording) binding.waveformView.idleWave()
                binding.waveformView.postDelayed(this, 140)
            }
        }, 140)
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is UiState.Idle -> Unit
                is UiState.Loading -> {
                    binding.loading.visibility = View.VISIBLE
                    binding.resultCard.visibility = View.GONE
                    binding.recordingStatus.text = "Analyzing MFCC voice features..."
                }
                is UiState.Success -> {
                    binding.loading.visibility = View.GONE
                    binding.resultCard.visibility = View.VISIBLE
                    binding.txtVerdict.text = state.data.verdict
                    binding.txtConfidence.text = "Confidence: ${state.data.confidence}% · Risk: ${state.data.risk_level}"
                    binding.confidenceProgress.progress = state.data.confidence.toInt()
                    binding.txtExplanation.text = state.data.explanation
                    revealResult()
                }
                is UiState.Error -> {
                    binding.loading.visibility = View.GONE
                    binding.recordingStatus.text = state.message
                }
            }
        }
    }

    private fun copyUriToCache(uri: Uri): File {
        val name = getFileName(uri) ?: "uploaded_audio_${System.currentTimeMillis()}.m4a"
        val file = File(cacheDir, name)
        contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        return file
    }

    private fun getFileName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) return cursor.getString(idx)
        }
        return null
    }

    private fun animateTap(view: View) {
        AnimatorSet().apply {
            playTogether(ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.96f, 1f), ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.96f, 1f))
            duration = 210
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun startEntranceAnimations() {
        listOf(binding.badge, binding.title, binding.waveformView, binding.btnRecord, binding.btnAnalyze).forEachIndexed { index, v ->
            v.alpha = 0f
            v.translationY = 40f
            v.animate().alpha(1f).translationY(0f).setStartDelay((index * 90).toLong()).setDuration(520).start()
        }
    }

    private fun revealResult() {
        binding.resultCard.alpha = 0f
        binding.resultCard.translationY = 45f
        binding.resultCard.scaleX = 0.96f
        binding.resultCard.scaleY = 0.96f
        binding.resultCard.animate().alpha(1f).translationY(0f).scaleX(1f).scaleY(1f).setDuration(520).setInterpolator(DecelerateInterpolator()).start()
    }
}
