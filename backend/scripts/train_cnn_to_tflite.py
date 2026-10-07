from pathlib import Path
import numpy as np
from app.audio_features import extract_mfcc

DATASET = Path("dataset")
MODEL_DIR = Path("models")
MODEL_DIR.mkdir(exist_ok=True)

def load_dataset():
    X, y = [], []
    for label_name, label in [("real", 0), ("fake", 1)]:
        for p in (DATASET / label_name).glob("*"):
            if p.suffix.lower() in [".wav", ".mp3", ".m4a", ".flac", ".ogg"]:
                X.append(extract_mfcc(p))
                y.append(label)
    X = np.array(X, dtype="float32")[..., np.newaxis]
    y = np.array(y, dtype="float32")
    return X, y

def main():
    import tensorflow as tf
    X, y = load_dataset()
    if len(X) < 2:
        raise RuntimeError("Add audio files in dataset/real and dataset/fake first.")
    model = tf.keras.Sequential([
        tf.keras.layers.Input(shape=X.shape[1:]),
        tf.keras.layers.Conv2D(32, 3, activation="relu"),
        tf.keras.layers.MaxPooling2D(),
        tf.keras.layers.Conv2D(64, 3, activation="relu"),
        tf.keras.layers.MaxPooling2D(),
        tf.keras.layers.GlobalAveragePooling2D(),
        tf.keras.layers.Dropout(0.25),
        tf.keras.layers.Dense(64, activation="relu"),
        tf.keras.layers.Dense(1, activation="sigmoid"),
    ])
    model.compile(optimizer="adam", loss="binary_crossentropy", metrics=["accuracy"])
    model.fit(X, y, epochs=15, validation_split=0.2, batch_size=8)
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    (MODEL_DIR / "model.tflite").write_bytes(converter.convert())
    print("Saved models/model.tflite")

if __name__ == "__main__":
    main()
