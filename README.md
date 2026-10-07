# Deepfake / Fake Audio Detection System

A portfolio-ready Android + AI/ML project for detecting fake/deepfake audio from recorded or uploaded clips.

## Tech Stack

- Android: Kotlin, XML UI, ViewBinding, MVVM, MediaRecorder, Canvas API
- Backend: FastAPI, Librosa, NumPy
- ML: MFCC feature extraction, CNN/TensorFlow Lite compatible inference
- API: Multipart audio upload returning REAL/FAKE verdict with confidence score

## Features

- Record audio from Android app
- Upload audio from device
- Custom `WaveformView` using Android Canvas API
- Smooth premium dark UI
- Multipart upload to FastAPI backend
- Librosa MFCC preprocessing
- Demo classifier works immediately without dataset
- Optional CNN to TFLite training script
- Returns verdict, confidence, fake probability, risk level, explanation

## Run backend

```powershell
cd backend
python -m venv venv
.\venv\Scripts\Activate.ps1
python -m pip install --upgrade pip setuptools wheel
python -m pip install -r requirements.txt
python -m uvicorn app.main:app --reload --port 8000
```

Check:

```text
http://localhost:8000/health
```

## Run Android

Open Android Studio → Open the `android` folder.

Backend URL file:

```text
android/app/src/main/java/com/saksham/deepfakeaudio/api/ApiClient.kt
```

Emulator URL:

```kotlin
http://10.0.2.2:8000/
```

Physical phone URL:

```kotlin
http://YOUR_PC_IPV4:8000/
```

## Important

This project includes a demo MFCC heuristic so the app works without a trained model. For production, train the CNN using a real dataset and place `model.tflite` inside:

```text
backend/models/model.tflite
```

## Resume Bullet

```text
Deepfake / Fake Audio Detection System | Kotlin, TensorFlow Lite, FastAPI, Librosa, CNN
• Built an end-to-end Android app for recording/uploading audio clips and detecting fake voices.
• Implemented Librosa-based MFCC preprocessing and FastAPI multipart upload pipeline.
• Developed custom Canvas-based WaveformView and smooth MVVM Android UI with REAL/FAKE verdict and confidence score.
```
