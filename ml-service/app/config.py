#from dataclasses import dataclass
from typing import Final

HOST: Final[str] = "0.0.0.0"
PORT: Final[int] = 8000
LOG_LEVEL: Final[str] = "info" #or debug

APP_TITLE: Final[str] = "App Title"
SERVICE_KEY: Final[str] = "secret_key"

ALLOWED_TYPES : Final[ set[str] ] = {
    "image/jpeg",
    "image/png"
}

MAX_FILE_SIZE = 5 * 1024 * 1024

# @dataclass(frozen=True)
# class ServerConfig:
#     host: str = "0.0.0.0"
#     port: int = 8000
#     log_level: str = "info"


# @dataclass(frozen=True)
# class AppConfig:
#     title: str = "App Title"


# @dataclass(frozen=True)
# class Config:
#     server: ServerConfig = ServerConfig()
#     app: AppConfig = AppConfig()


#config = Config()