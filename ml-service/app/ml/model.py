
import json
from pathlib import Path

import torch
import torch.nn as nn
from torchvision import models, transforms
from PIL import Image


class PlantDiseaseModel:
    def __init__(self):
        # Find the saved model files
        project_root = Path(__file__).resolve().parents[2]
        model_dir = project_root / "models"

        self.model_path = model_dir / "plant_disease_model.pth"
        self.classes_path = model_dir / "class_names.json"

        # Use CPU or GPU automatically
        self.device = torch.device(
            "cuda" if torch.cuda.is_available() else "cpu"
        )

        # Load the class names
        with open(self.classes_path, "r", encoding="utf-8") as file:
            self.class_names = json.load(file)

        # Create MobileNetV2 with the same 10 output classes
        self.model = models.mobilenet_v2(weights=None)
        self.model.classifier[1] = nn.Linear(
            self.model.last_channel,
            len(self.class_names),
        )

        # Load the trained weights
        state = torch.load(
            self.model_path,
            map_location=self.device,
            weights_only=True,
        )
        self.model.load_state_dict(state)
        self.model.to(self.device)
        self.model.eval()

        # Preprocess images in the same way as training
        self.transform = transforms.Compose([
            transforms.Resize((224, 224)),
            transforms.ToTensor(),
            transforms.Normalize(
                mean=[0.485, 0.456, 0.406],
                std=[0.229, 0.224, 0.225],
            ),
        ])

    def predict(self, image: Image.Image) -> dict:
        # Ensure the image has three color channels
        image = image.convert("RGB")

        # Prepare the image for the model
        image_tensor = self.transform(image).unsqueeze(0)
        image_tensor = image_tensor.to(self.device)

        # Predict the class
        with torch.no_grad():
            outputs = self.model(image_tensor)
            probabilities = torch.softmax(outputs, dim=1)
            confidence, predicted_index = probabilities.max(dim=1)

        # Convert the predicted class name into crop and disease
        class_name = self.class_names[predicted_index.item()]
        crop, disease = class_name.split("___", 1)

        crop = crop.replace("_", " ").replace(",", "")
        disease = disease.replace("_", " ").strip()

        return {
            "crop": crop,
            "disease": disease,
            "confidence": round(confidence.item(), 4),
        }

