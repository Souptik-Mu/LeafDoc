import json
from pathlib import Path
import torch
import torch.nn as nn
from torchvision import models, transforms # type: ignore
from PIL import Image
from dto.types import PredictionItemDTO
class PlantDiseaseModel:
    MODEL_NAME = "LeafDoc-MobileNetV2"
    MODEL_VERSION = "2.0.0"
    INPUT_SIZE = 224

    CLASS_MAPPING = {

            # --------------------------------
            # TOMATO - 10 CLASSES
            # --------------------------------

            "Tomato___Bacterial_spot": {
                "crop_code": "TOMATO",
                "crop_name": "Tomato",
                "disease_label": "TOMATO_BACTERIAL_SPOT",
                "disease_code": "BACTERIAL_SPOT",
                "disease_name": "Bacterial Spot",
                "is_healthy": False,
            },

            "Tomato___Early_blight": {
                "crop_code": "TOMATO",
                "crop_name": "Tomato",
                "disease_label": "TOMATO_EARLY_BLIGHT",
                "disease_code": "EARLY_BLIGHT",
                "disease_name": "Early Blight",
                "is_healthy": False,
            },

            "Tomato___healthy": {
                "crop_code": "TOMATO",
                "crop_name": "Tomato",
                "disease_label": "TOMATO_HEALTHY",
                "disease_code": "HEALTHY",
                "disease_name": "Healthy",
                "is_healthy": True,
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

            "Tomato___Spider_mites Two-spotted_spider_mite": {
                "crop_code": "TOMATO",
                "crop_name": "Tomato",
                "disease_label": "TOMATO_SPIDER_MITES",
                "disease_code": "SPIDER_MITES",
                "disease_name": "Spider Mites (Two-Spotted Spider Mite)",
                "is_healthy": False,
            },

            "Tomato___Target_Spot": {
                "crop_code": "TOMATO",
                "crop_name": "Tomato",
                "disease_label": "TOMATO_TARGET_SPOT",
                "disease_code": "TARGET_SPOT",
                "disease_name": "Target Spot",
                "is_healthy": False,
            },

            "Tomato___Tomato_mosaic_virus": {
                "crop_code": "TOMATO",
                "crop_name": "Tomato",
                "disease_label": "TOMATO_MOSAIC_VIRUS",
                "disease_code": "MOSAIC_VIRUS",
                "disease_name": "Tomato Mosaic Virus",
                "is_healthy": False,
            },

            "Tomato___Tomato_Yellow_Leaf_Curl_Virus": {
                "crop_code": "TOMATO",
                "crop_name": "Tomato",
                "disease_label": "TOMATO_YELLOW_LEAF_CURL_VIRUS",
                "disease_code": "YELLOW_LEAF_CURL_VIRUS",
                "disease_name": "Tomato Yellow Leaf Curl Virus",
                "is_healthy": False,
            },

            # --------------------------------
            # POTATO - 3 CLASSES
            # --------------------------------

            "Potato___Early_blight": {
                "crop_code": "POTATO",
                "crop_name": "Potato",
                "disease_label": "POTATO_EARLY_BLIGHT",
                "disease_code": "EARLY_BLIGHT",
                "disease_name": "Early Blight",
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

            "Potato___Late_blight": {
                "crop_code": "POTATO",
                "crop_name": "Potato",
                "disease_label": "POTATO_LATE_BLIGHT",
                "disease_code": "LATE_BLIGHT",
                "disease_name": "Late Blight",
                "is_healthy": False,
            },

            # --------------------------------
            # BELL PEPPER - 2 CLASSES
            # --------------------------------

            "Pepper,_bell___Bacterial_spot": {
                "crop_code": "PEPPER_BELL",
                "crop_name": "Bell Pepper",
                "disease_label": "PEPPER_BELL_BACTERIAL_SPOT",
                "disease_code": "BACTERIAL_SPOT",
                "disease_name": "Bacterial Spot",
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

            # --------------------------------
            # APPLE - 4 CLASSES
            # --------------------------------

            "Apple___Apple_scab": {
                "crop_code": "APPLE",
                "crop_name": "Apple",
                "disease_label": "APPLE_SCAB",
                "disease_code": "APPLE_SCAB",
                "disease_name": "Apple Scab",
                "is_healthy": False,
            },

            "Apple___Black_rot": {
                "crop_code": "APPLE",
                "crop_name": "Apple",
                "disease_label": "APPLE_BLACK_ROT",
                "disease_code": "BLACK_ROT",
                "disease_name": "Black Rot",
                "is_healthy": False,
            },

            "Apple___Cedar_apple_rust": {
                "crop_code": "APPLE",
                "crop_name": "Apple",
                "disease_label": "APPLE_CEDAR_APPLE_RUST",
                "disease_code": "CEDAR_APPLE_RUST",
                "disease_name": "Cedar Apple Rust",
                "is_healthy": False,
            },

            "Apple___healthy": {
                "crop_code": "APPLE",
                "crop_name": "Apple",
                "disease_label": "APPLE_HEALTHY",
                "disease_code": "HEALTHY",
                "disease_name": "Healthy",
                "is_healthy": True,
            },

            # --------------------------------
            # PEACH - 2 CLASSES
            # --------------------------------

            "Peach___Bacterial_spot": {
                "crop_code": "PEACH",
                "crop_name": "Peach",
                "disease_label": "PEACH_BACTERIAL_SPOT",
                "disease_code": "BACTERIAL_SPOT",
                "disease_name": "Bacterial Spot",
                "is_healthy": False,
            },

            "Peach___healthy": {
                "crop_code": "PEACH",
                "crop_name": "Peach",
                "disease_label": "PEACH_HEALTHY",
                "disease_code": "HEALTHY",
                "disease_name": "Healthy",
                "is_healthy": True,
            },

            # --------------------------------
            # SOYBEAN - 1 CLASS
            # --------------------------------

            "Soybean___healthy": {
                "crop_code": "SOYBEAN",
                "crop_name": "Soybean",
                "disease_label": "SOYBEAN_HEALTHY",
                "disease_code": "HEALTHY",
                "disease_name": "Healthy",
                "is_healthy": True,
            },

            # --------------------------------
            # ORANGE - 1 CLASS
            # --------------------------------

            "Orange___Haunglongbing_(Citrus_greening)": {
                "crop_code": "ORANGE",
                "crop_name": "Orange",
                "disease_label": "ORANGE_CITRUS_GREENING",
                "disease_code": "CITRUS_GREENING",
                "disease_name": "Huanglongbing (Citrus Greening)",
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
        self.class_names = self._load_classes()

        # Load trained model
        self.model = self._load_model()

        # Prepare image transformation
        self.transform = self._build_transform()

    def _load_classes(self) -> list[str]:
            with open(self.classes_path, "r", encoding="utf-8") as file:
                    classes = json.load(file)
             if not classes:
                      raise ValueError("No model classes found")
              return classes

        with open(self.classes_path, "r", encoding="utf-8") as file:
            classes = json.load(file)

        # Validate number of classes
        if len(classes) != 23:
            raise ValueError(
                f"Expected 23 classes, found {len(classes)}"
            )

        # Check for duplicate classes
        if len(set(classes)) != len(classes):
            raise ValueError("Duplicate class names found")

        # Validate class mappings
        for class_name in classes:
            if class_name not in self.CLASS_MAPPING:
                raise ValueError(
                    f"Missing class mapping: {class_name}"
                )

        return classes

    def _load_model(self):

        model = models.mobilenet_v2(weights=None)

        model.classifier[1] = nn.Linear(
            model.last_channel,
            len(self.class_names),
        )

        # Load trained weights
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
        image_tensor: torch.Tensor = (
            self.transform(image).unsqueeze(0).to(self.device)
        )  # type: ignore

        # Run model prediction
        outputs: torch.Tensor = self.model(image_tensor)
        probabilities: torch.Tensor = torch.softmax(outputs, dim=1)

        # Ensure probabilities are valid
        if not torch.isfinite(probabilities).all().item():
            raise RuntimeError("Invalid prediction probabilities")

        # Get top 3 predictions
        top_values, top_indices = torch.topk(
            probabilities[0],
            k=min(3, len(self.class_names))
        )

        scores: list[float] = top_values.tolist()  # type: ignore
        indices: list[int] = top_indices.tolist()  # type: ignore

        # Return prediction results
        return [
            PredictionItemDTO(
                classLabel=self.class_names[index],
                confidenceScore=round(score, 4)
            )
            for score, index in zip(scores, indices)
        ]
