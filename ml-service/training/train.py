
import json
from pathlib import Path

import torch
import torch.nn as nn
from torch.utils.data import DataLoader, Subset
from torchvision import datasets, transforms, models
from sklearn.model_selection import train_test_split


# --------------------------------------------------
# 1. PROJECT PATHS
# --------------------------------------------------

PROJECT_ROOT = Path(__file__).resolve().parents[2]

DATASET_DIR = (
    PROJECT_ROOT
    / "dataset"
    / "plantvillage dataset"
    / "segmented"
)

MODEL_DIR = PROJECT_ROOT / "ml-service" / "models"


# --------------------------------------------------
# 2. SELECTED DISEASE CLASSES
# --------------------------------------------------

CLASSES = [
    "Tomato___healthy",
    "Tomato___Early_blight",
    "Tomato___Late_blight",
    "Tomato___Leaf_Mold",
    "Tomato___Septoria_leaf_spot",
    "Potato___healthy",
    "Potato___Early_blight",
    "Potato___Late_blight",
    "Pepper,_bell___healthy",
    "Pepper,_bell___Bacterial_spot",
]

IMAGE_SIZE = 224
BATCH_SIZE = 16
EPOCHS = 5
SEED = 42


# --------------------------------------------------
# 3. TRAINING FUNCTION
# --------------------------------------------------

def main():

    torch.manual_seed(SEED)

    if not DATASET_DIR.is_dir():
        raise FileNotFoundError(
            f"Dataset folder not found: {DATASET_DIR}"
        )

    MODEL_DIR.mkdir(parents=True, exist_ok=True)

    device = torch.device(
        "cuda" if torch.cuda.is_available() else "cpu"
    )

    print("Using device:", device)
    print("Dataset path:", DATASET_DIR)
    print("Loading dataset...")

    # Image transformations
    transform = transforms.Compose([
        transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
        transforms.RandomHorizontalFlip(),
        transforms.ToTensor(),
        transforms.Normalize(
            mean=[0.485, 0.456, 0.406],
            std=[0.229, 0.224, 0.225],
        ),
    ])

    # Load all dataset folders
    full_dataset = datasets.ImageFolder(
        root=str(DATASET_DIR),
        transform=transform,
    )

    # Check that all selected classes exist
    missing_classes = [
        name for name in CLASSES
        if name not in full_dataset.class_to_idx
    ]

    if missing_classes:
        raise ValueError(
            f"Missing dataset classes: {missing_classes}"
        )

    # Map selected class names to output labels 0-9
    class_to_new_index = {
        class_name: index
        for index, class_name in enumerate(CLASSES)
    }

    # Select images belonging only to our 10 classes
    selected_indices = [
        index
        for index, (_, original_label) in enumerate(
            full_dataset.samples
        )
        if full_dataset.classes[original_label] in class_to_new_index
    ]

    # Create labels in the new 0-9 order
    selected_labels = [
        class_to_new_index[
            full_dataset.classes[full_dataset.samples[index][1]]
        ]
        for index in selected_indices
    ]

    print("Selected classes:", len(CLASSES))
    print("Selected images:", len(selected_indices))

    # Split indices while preserving class proportions
    train_indices, val_indices = train_test_split(
        list(range(len(selected_indices))),
        test_size=0.2,
        random_state=SEED,
        stratify=selected_labels,
    )

    # Dataset that returns remapped labels
    class PlantDiseaseDataset(torch.utils.data.Dataset):

        def __init__(self, indices):
            self.indices = indices

        def __len__(self):
            return len(self.indices)

        def __getitem__(self, item):
            selected_position = self.indices[item]
            original_index = selected_indices[selected_position]

            image, _ = full_dataset[original_index]
            label = selected_labels[selected_position]

            return image, label

    train_dataset = PlantDiseaseDataset(train_indices)
    val_dataset = PlantDiseaseDataset(val_indices)

    train_loader = DataLoader(
        train_dataset,
        batch_size=BATCH_SIZE,
        shuffle=True,
        num_workers=0,
    )

    val_loader = DataLoader(
        val_dataset,
        batch_size=BATCH_SIZE,
        shuffle=False,
        num_workers=0,
    )

    print("Training images:", len(train_dataset))
    print("Validation images:", len(val_dataset))

    # --------------------------------------------------
    # 4. LOAD MOBILENETV2
    # --------------------------------------------------

    print("Loading pretrained MobileNetV2...")

    weights = models.MobileNet_V2_Weights.DEFAULT

    model = models.mobilenet_v2(weights=weights)

    # Freeze pretrained feature-extraction layers
    for parameter in model.features.parameters():
        parameter.requires_grad = False

    # Replace classifier with 10 output classes
    model.classifier[1] = nn.Linear(
        model.last_channel,
        len(CLASSES),
    )

    model = model.to(device)

    criterion = nn.CrossEntropyLoss()

    optimizer = torch.optim.Adam(
        model.classifier.parameters(),
        lr=0.001,
    )

    # --------------------------------------------------
    # 5. TRAIN AND VALIDATE
    # --------------------------------------------------

    best_accuracy = 0.0

    for epoch in range(EPOCHS):

        model.train()
        total_loss = 0.0

        for images, labels in train_loader:

            images = images.to(device)
            labels = labels.to(device)

            optimizer.zero_grad()

            outputs = model(images)
            loss = criterion(outputs, labels)

            loss.backward()
            optimizer.step()

            total_loss += loss.item() * images.size(0)

        average_loss = total_loss / len(train_dataset)

        # Evaluate on validation data
        model.eval()

        correct = 0
        total = 0

        with torch.no_grad():

            for images, labels in val_loader:

                images = images.to(device)
                labels = labels.to(device)

                outputs = model(images)
                predictions = outputs.argmax(dim=1)

                correct += (
                    predictions == labels
                ).sum().item()

                total += labels.size(0)

        accuracy = correct / total if total else 0.0

        print(
            f"Epoch {epoch + 1}/{EPOCHS} | "
            f"Loss: {average_loss:.4f} | "
            f"Validation accuracy: {accuracy:.2%}"
        )

        # Save the best model weights
        if accuracy > best_accuracy:
            best_accuracy = accuracy

            torch.save(
                model.state_dict(),
                MODEL_DIR / "plant_disease_model.pth",
            )

    # --------------------------------------------------
    # 6. SAVE CLASS LABELS
    # --------------------------------------------------

    with open(
        MODEL_DIR / "class_names.json",
        "w",
        encoding="utf-8",
    ) as file:
        json.dump(CLASSES, file, indent=4)

    print("\nTraining completed.")
    print("Best validation accuracy:", f"{best_accuracy:.2%}")
    print("Model saved to:", MODEL_DIR / "plant_disease_model.pth")
    print("Class names saved to:", MODEL_DIR / "class_names.json")


if __name__ == "__main__":
    main()
