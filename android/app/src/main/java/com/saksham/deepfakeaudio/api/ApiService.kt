package com.saksham.deepfakeaudio.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("predict-audio")
    suspend fun predictAudio(@Part file: MultipartBody.Part): Response<PredictionResponse>
}
