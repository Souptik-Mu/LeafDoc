from pathlib import Path

import torch
import torch.nn as nn
from torchvision import models, transforms # type: ignore
from PIL import Image
from dto.types import PredictionItemDTO 

class PlantDiseaseModel:

    MODEL_NAME = "LeafDoc-MobileNetV2"
    MODEL_VERSION = "1.0.0"
    INPUT_SIZE = 224

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

        self.class_names = self._load_classes()
        self.model = self._load_model()
        self.transform = self._build_transform()
        
    def _load_classes(self) -> list[str]:
        import json
        with open(self.classes_path, encoding="utf-8") as file:
            classes = json.load(file)
        if not classes:
            raise ValueError("No model classes found")
        return classes
    
    def _load_model(self):
        model = models.mobilenet_v2(weights=None)
        model.classifier[1] = nn.Linear(
            model.last_channel,
            len(self.class_names),
        )
        state = torch.load(
            self.model_path,
            map_location=self.device,
            weights_only=True,
        )
        model.load_state_dict(state)
        model.to(self.device)
        model.eval()
        return model
    
    def _build_transform(self):
        return transforms.Compose([
            transforms.Resize(
                (self.INPUT_SIZE, self.INPUT_SIZE)
            ),
            transforms.ToTensor(),
            transforms.Normalize(
                mean=[0.485, 0.456, 0.406],
                std=[0.229, 0.224, 0.225],
            ),
        ])

    @torch.inference_mode()
    def predict(self, image: Image.Image) -> list[PredictionItemDTO]:
        # Ensure the image has three color channels
        image = image.convert("RGB")

        # Preprocess image
        image_tensor : torch.Tensor = self.transform(image).unsqueeze(0).to(self.device) # type: ignore

        # Run model prediction
        #with torch.inference_mode(): # with decorator
        outputs : torch.Tensor = self.model(image_tensor)
        probabilities : torch.Tensor = torch.softmax(outputs, dim=1)

        # Ensure probabilities are valid
        if not torch.isfinite(probabilities).all().item():
            raise RuntimeError("Invalid prediction probabilities")

        # Get top 3 predictions
        top_values , top_indices = torch.topk(
            probabilities[0],
            k=min(3, len(self.class_names))
        )
        scores: list[float] = top_values.tolist() # pyright: ignore[reportUnknownVariableType, reportUnknownMemberType]
        indices: list[int] = top_indices.tolist() # pyright: ignore[reportUnknownVariableType, reportUnknownMemberType]

        return [
            PredictionItemDTO(
                classLabel= self.class_names[index],
                confidenceScore= round(score, 4)
            )for score, index in zip(scores, indices)
        ]

