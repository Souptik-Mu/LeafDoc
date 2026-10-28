
import uvicorn
import cv2 as cv

from PIL import Image
from fastapi import (
    FastAPI,
    HTTPException,
    Request,
    Response,
    UploadFile,
    File,
    Header,
    Depends,
)
from fastapi.responses import JSONResponse

import config
from imageProcessor import ImageProcessor
from ml.model import PlantDiseaseModel


class Server:
    def __init__(self):
        self.app = FastAPI(title=config.APP_TITLE)
        self.image_processor = ImageProcessor()
        self.predictor = PlantDiseaseModel()
        self._configure_routes()

        self.app.add_exception_handler(
            Exception,
            self._defaultExceptionHandler,
        )

    def _configure_routes(self):
        self.app.add_api_route(
            "/ml/predict",
            self.predict,
            methods=["POST"],
        )
        self.app.add_api_route(
            "/ml",
            self.get,
            methods=["GET"],
        )
        self.app.add_api_route(
            "/ml/health",
            self.health,
            methods=["GET", "POST", "OPTIONS"],
        )

    @staticmethod
    def _defaultExceptionHandler(
        request: Request,
        exc: Exception,
    ) -> Response:
        return JSONResponse(
            status_code=500,
            content={
                "detail": "Something went wrong",
                "type": type(exc).__name__,
                "msg": str(exc),
            },
        )

    @staticmethod
    def _validate_service_key(
        x_ml_service_key: str = Header(...),
    ):
        if x_ml_service_key != config.SERVICE_KEY:
            raise HTTPException(
                status_code=403,
                detail="Unauthorized access",
            )

    async def predict(
        self,
        file: UploadFile = File(...),
        _: None = Depends(_validate_service_key),
    ) -> Response:

        if file.content_type not in config.ALLOWED_TYPES:
            await file.close()
            raise HTTPException(
                status_code=400,
                detail="Only JPEG and PNG images are allowed",
            )

        data = await file.read()
        await file.close()

        if not data:
            raise HTTPException(
                status_code=400,
                detail="Uploaded file is empty",
            )

        if len(data) > config.MAX_FILE_SIZE:
            raise HTTPException(
                status_code=413,
                detail="Image exceeds the 5 MB size limit",
            )

        try:
            image_bgr = self.image_processor.decode(data)

            image_rgb = cv.cvtColor(
                image_bgr,
                cv.COLOR_BGR2RGB,
            )

            pil_image = Image.fromarray(image_rgb)

            prediction = self.predictor.predict(pil_image)

            return JSONResponse(
                status_code=200,
                content=prediction,
            )

        except ValueError as exc:
            raise HTTPException(
                status_code=400,
                detail=str(exc),
            ) from exc

    async def health(
        self,
        _: None = Depends(_validate_service_key),
    ) -> dict:
        return {
            "service": "LeafDoc ML Service",
            "status": "Connected",
            "authentication": "Valid",
        }

    async def get(self) -> dict:
        return {
            "msg": "Hello from ML service",
            "status": "Connected",
        }
    def start(self) -> None:
        uvicorn.run(
            self.app,
            host=config.HOST,
            port=config.PORT,
            log_level=config.LOG_LEVEL,
        )
