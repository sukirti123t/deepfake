package com.saksham.deepfakeaudio.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saksham.deepfakeaudio.api.ApiClient
import com.saksham.deepfakeaudio.api.PredictionResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val data: PredictionResponse) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel : ViewModel() {
    val state = MutableLiveData<UiState>(UiState.Idle)

    fun analyze(file: File) {
        state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val request = file.asRequestBody("audio/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, request)
                val response = ApiClient.service.predictAudio(part)
                state.value = if (response.isSuccessful && response.body() != null) {
                    UiState.Success(response.body()!!)
                } else UiState.Error(response.errorBody()?.string() ?: "Prediction failed")
            } catch (e: Exception) {
                state.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }
}
