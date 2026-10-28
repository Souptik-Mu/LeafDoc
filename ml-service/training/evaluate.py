
import json
from pathlib import Path

import torch
from torch.utils.data import DataLoader, Subset
from torchvision import datasets, transforms, models
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix,
)

PROJECT_ROOT = Path(__file__).resolve().parents[2]
DATASET_DIR = PROJECT_ROOT / "dataset" / "plantvillage dataset" / "segmented"
MODEL_DIR = PROJECT_ROOT / "ml-service" / "models"

IMAGE_SIZE = 224
BATCH_SIZE = 16

def main():
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    with open(MODEL_DIR / "class_names.json", encoding="utf-8") as f:
        class_names = json.load(f)

    transform = transforms.Compose([
        transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
        transforms.ToTensor(),
        transforms.Normalize(
            mean=[0.485, 0.456, 0.406],
            std=[0.229, 0.224, 0.225],
        ),
    ])

    dataset = datasets.ImageFolder(str(DATASET_DIR), transform=transform)

    # Select only the 10 classes used during training
    selected_indices = [
        i for i, (_, label) in enumerate(dataset.samples)
        if dataset.classes[label] in class_names
    ]

    # Recreate the same validation split used during training
    from sklearn.model_selection import train_test_split

    selected_labels = [
        class_names.index(dataset.classes[dataset.samples[i][1]])
        for i in selected_indices
    ]

    _, val_positions = train_test_split(
        list(range(len(selected_indices))),
        test_size=0.2,
        random_state=42,
        stratify=selected_labels,
    )

    val_indices = [selected_indices[i] for i in val_positions]
    val_dataset = Subset(dataset, val_indices)
    loader = DataLoader(
        val_dataset, batch_size=BATCH_SIZE, shuffle=False, num_workers=0
    )

    model = models.mobilenet_v2(weights=None)
    model.classifier[1] = torch.nn.Linear(model.last_channel, len(class_names))

    state = torch.load(
        MODEL_DIR / "plant_disease_model.pth",
        map_location=device,
        weights_only=True,
    )
    model.load_state_dict(state)
    model.to(device)
    model.eval()

    y_true = []
    y_pred = []

    with torch.no_grad():
        for images, original_labels in loader:
            # Convert original ImageFolder labels into our 10-class labels
            labels = [
                class_names.index(dataset.classes[label.item()])
                for label in original_labels
            ]

            outputs = model(images.to(device))
            predictions = outputs.argmax(dim=1).cpu().tolist()

            y_true.extend(labels)
            y_pred.extend(predictions)

    print("\nEvaluation Results")
    print("------------------")
    print(f"Accuracy: {accuracy_score(y_true, y_pred):.2%}")

    print("\nPrecision, Recall and F1-score:")
    print(classification_report(
        y_true,
        y_pred,
        labels=list(range(len(class_names))),
        target_names=class_names,
        zero_division=0,
    ))

    print("\nConfusion Matrix:")
    print(confusion_matrix(y_true, y_pred, labels=list(range(len(class_names)))))

if __name__ == "__main__":
    main()

