import uuid
from pathlib import Path
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from .model_service import detector

BASE_DIR = Path(__file__).resolve().parents[1]
UPLOAD_DIR = BASE_DIR / "uploads"
UPLOAD_DIR.mkdir(exist_ok=True)

app = FastAPI(
    title="Deepfake / Fake Audio Detection API",
    description="FastAPI backend for audio deepfake detection using Librosa MFCC preprocessing.",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

ALLOWED_EXT = {".wav", ".mp3", ".m4a", ".aac", ".flac", ".ogg"}

@app.get("/")
def root():
    return {"message": "Deepfake Audio Detection API running", "endpoint": "POST /predict-audio", "model_mode": detector.mode}

@app.get("/health")
def health():
    return {"status": "ok", "model_mode": detector.mode}

@app.post("/predict-audio")
async def predict_audio(file: UploadFile = File(...)):
    suffix = Path(file.filename or "").suffix.lower()
    if suffix not in ALLOWED_EXT:
        raise HTTPException(status_code=400, detail=f"Unsupported file type: {suffix}")

    target = UPLOAD_DIR / f"{uuid.uuid4().hex}{suffix}"
    content = await file.read()
    if len(content) < 100:
        raise HTTPException(status_code=400, detail="Audio file is empty or too small.")
    target.write_bytes(content)

    try:
        result = detector.predict(target)
        return {
            "file_name": file.filename,
            "verdict": result["label"],
            "confidence": result["confidence"],
            "fake_probability": result["fake_probability"],
            "risk_level": result["risk_level"],
            "model_mode": result["model_mode"],
            "audio_features": result["audio_features"],
            "explanation": result["explanation"],
        }
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"Prediction failed: {exc}")
