package com.saksham.deepfakeaudio.api

data class PredictionResponse(
    val file_name: String?,
    val verdict: String,
    val confidence: Double,
    val fake_probability: Double,
    val risk_level: String,
    val model_mode: String,
    val explanation: String
)
