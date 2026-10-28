
from typing import Final

HOST: Final[str] = "0.0.0.0"
PORT: Final[int] = 8000
LOG_LEVEL: Final[str] = "info"

APP_TITLE: Final[str] = "LeafDoc ML Service"
SERVICE_KEY: Final[str] = "secret_key"

ALLOWED_TYPES: Final[set[str]] = {
    "image/jpeg",
    "image/png",
}

MAX_FILE_SIZE: Final[int] = 5 * 1024 * 1024
