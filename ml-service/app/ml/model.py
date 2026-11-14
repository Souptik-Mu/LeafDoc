
import json
import math
from pathlib import Path

import torch
import torch.nn as nn
from torchvision import models, transforms
from PIL import Image


class PlantDiseaseModel:

    MODEL_NAME = "LeafDoc-MobileNetV2"
    MODEL_VERSION = "1.0.0"
    INPUT_SIZE = 224

    # Standard labels for the 10 trained classes
    CLASS_MAPPING = {
        "Tomato___healthy": {
            "crop_code": "TOMATO",
            "crop_name": "Tomato",
            "disease_label": "TOMATO_HEALTHY",
            "disease_code": "HEALTHY",
            "disease_name": "Healthy",
            "is_healthy": True,
        },
        "Tomato___Early_blight": {
            "crop_code": "TOMATO",
            "crop_name": "Tomato",
            "disease_label": "TOMATO_EARLY_BLIGHT",
            "disease_code": "EARLY_BLIGHT",
            "disease_name": "Early Blight",
            "is_healthy": False,
        },
        "Tomato___Late_blight": {
            "crop_code": "TOMATO",
            "crop_name": "Tomato",
            "disease_label": "TOMATO_LATE_BLIGHT",
            "disease_code": "LATE_BLIGHT",
            "disease_name": "Late Blight",
            "is_healthy": False,
        },
        "Tomato___Leaf_Mold": {
            "crop_code": "TOMATO",
            "crop_name": "Tomato",
            "disease_label": "TOMATO_LEAF_MOLD",
            "disease_code": "LEAF_MOLD",
            "disease_name": "Leaf Mold",
            "is_healthy": False,
        },
        "Tomato___Septoria_leaf_spot": {
            "crop_code": "TOMATO",
            "crop_name": "Tomato",
            "disease_label": "TOMATO_SEPTORIA_LEAF_SPOT",
            "disease_code": "SEPTORIA_LEAF_SPOT",
            "disease_name": "Septoria Leaf Spot",
            "is_healthy": False,
        },
        "Potato___healthy": {
            "crop_code": "POTATO",
            "crop_name": "Potato",
            "disease_label": "POTATO_HEALTHY",
            "disease_code": "HEALTHY",
            "disease_name": "Healthy",
            "is_healthy": True,
        },
        "Potato___Early_blight": {
            "crop_code": "POTATO",
            "crop_name": "Potato",
            "disease_label": "POTATO_EARLY_BLIGHT",
            "disease_code": "EARLY_BLIGHT",
            "disease_name": "Early Blight",
            "is_healthy": False,
        },
        "Potato___Late_blight": {
            "crop_code": "POTATO",
            "crop_name": "Potato",
            "disease_label": "POTATO_LATE_BLIGHT",
            "disease_code": "LATE_BLIGHT",
            "disease_name": "Late Blight",
            "is_healthy": False,
        },
        "Pepper,_bell___healthy": {
            "crop_code": "PEPPER_BELL",
            "crop_name": "Bell Pepper",
            "disease_label": "PEPPER_BELL_HEALTHY",
            "disease_code": "HEALTHY",
            "disease_name": "Healthy",
            "is_healthy": True,
        },
        "Pepper,_bell___Bacterial_spot": {
            "crop_code": "PEPPER_BELL",
            "crop_name": "Bell Pepper",
            "disease_label": "PEPPER_BELL_BACTERIAL_SPOT",
            "disease_code": "BACTERIAL_SPOT",
            "disease_name": "Bacterial Spot",
            "is_healthy": False,
        },
    }

    def __init__(self):

        # Locate saved model files
        project_root = Path(__file__).resolve().parents[2]
        model_dir = project_root / "models"

        self.model_path = model_dir / "plant_disease_model.pth"
        self.classes_path = model_dir / "class_names.json"

        # Select CPU or GPU
        self.device = torch.device(
            "cuda" if torch.cuda.is_available() else "cpu"
        )

        # Load class names
        with open(self.classes_path, "r", encoding="utf-8") as file:
            self.class_names = json.load(file)

        # Check that every model class has a mapping
        for class_name in self.class_names:
            if class_name not in self.CLASS_MAPPING:
                raise ValueError(
                    f"Missing class mapping: {class_name}"
                )

        # Create MobileNetV2
        self.model = models.mobilenet_v2(weights=None)

        self.model.classifier[1] = nn.Linear(
            self.model.last_channel,
            len(self.class_names),
        )

        # Load trained weights
        state = torch.load(
            self.model_path,
            map_location=self.device,
            weights_only=True,
        )

        self.model.load_state_dict(state)
        self.model.to(self.device)
        self.model.eval()

        # Image preprocessing
        self.transform = transforms.Compose([
            transforms.Resize((self.INPUT_SIZE, self.INPUT_SIZE)),
            transforms.ToTensor(),
            transforms.Normalize(
                mean=[0.485, 0.456, 0.406],
                std=[0.229, 0.224, 0.225],
            ),
        ])

    def predict(self, image: Image.Image) -> dict[str,str]:
        # Ensure the image has three color channels
        image = image.convert("RGB")

        # Preprocess image
        image_tensor = self.transform(image).unsqueeze(0)
        image_tensor = image_tensor.to(self.device)

        # Run model prediction
        with torch.no_grad():
            outputs = self.model(image_tensor)
            probabilities = torch.softmax(outputs, dim=1)

        # Ensure probabilities are valid
        if not torch.isfinite(probabilities).all():
            raise RuntimeError("Invalid prediction probabilities")

        # Get top 3 predictions
        top_values, top_indices = torch.topk(
            probabilities[0],
            k=min(3, len(self.class_names))
        )

        # Get predicted class
        predicted_index = top_indices[0].item()
        predicted_class = self.class_names[predicted_index]

        predicted_info = self.CLASS_MAPPING[predicted_class]

        # Build top predictions
        top_predictions = []

        for score, index in zip(top_values, top_indices):

            class_name = self.class_names[index.item()]
            class_info = self.CLASS_MAPPING[class_name]

            top_predictions.append({
                "classLabel": class_info["disease_label"],
                "score": round(score.item(), 4),
            })

        # Final prediction
        confidence = top_values[0].item()

        confidance_percentage : int = round(confidence.item() *100) #softmax

        return {
            "crop": {
                "code": predicted_info["crop_code"],
                "name": predicted_info["crop_name"],
            },
            "disease": {
                "classLabel": predicted_info["disease_label"],
                "code": predicted_info["disease_code"],
                "name": predicted_info["disease_name"],
                "isHealthy": predicted_info["is_healthy"],
            },
            "confidenceScore": round(confidence, 4),
            "topPredictions": top_predictions,
            "modelInfo": {
                "modelName": self.MODEL_NAME,
                "modelVersion": self.MODEL_VERSION,
                "inputSize": self.INPUT_SIZE,
            },
        }
