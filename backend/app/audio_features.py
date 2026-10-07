from pathlib import Path
import numpy as np
import librosa

SAMPLE_RATE = 16000
N_MFCC = 40
MAX_SECONDS = 5
MAX_FRAMES = 160

def load_audio(path: str | Path):
    audio, sr = librosa.load(str(path), sr=SAMPLE_RATE, mono=True)
    max_len = SAMPLE_RATE * MAX_SECONDS
    if len(audio) > max_len:
        audio = audio[:max_len]
    if len(audio) < max_len:
        audio = np.pad(audio, (0, max_len - len(audio)))
    return audio, SAMPLE_RATE

def extract_mfcc(path: str | Path) -> np.ndarray:
    audio, sr = load_audio(path)
    mfcc = librosa.feature.mfcc(y=audio, sr=sr, n_mfcc=N_MFCC)
    mfcc = mfcc[:, :MAX_FRAMES]
    if mfcc.shape[1] < MAX_FRAMES:
        mfcc = np.pad(mfcc, ((0, 0), (0, MAX_FRAMES - mfcc.shape[1])))
    mfcc = (mfcc - np.mean(mfcc)) / (np.std(mfcc) + 1e-8)
    return mfcc.astype("float32")

def audio_stats(path: str | Path) -> dict:
    audio, sr = load_audio(path)
    rms = librosa.feature.rms(y=audio)[0]
    zcr = librosa.feature.zero_crossing_rate(audio)[0]
    centroid = librosa.feature.spectral_centroid(y=audio, sr=sr)[0]
    return {
        "duration_seconds": round(len(audio) / sr, 2),
        "rms_mean": float(np.mean(rms)),
        "zcr_mean": float(np.mean(zcr)),
        "spectral_centroid_mean": float(np.mean(centroid)),
    }
