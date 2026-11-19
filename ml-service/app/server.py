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
        # 1. Validate file type
        if file.content_type not in config.ALLOWED_TYPES:
            await file.close()
            raise HTTPException(
                status_code=400,
                detail="Only JPEG and PNG images are allowed",
            )
        # 2. Read uploaded image
        data = await file.read()
        await file.close()
        # 3. Validate empty file
        if not data:
            raise HTTPException(
                status_code=400,
                detail="Uploaded file is empty",
            )
        # 4. Validate file size
        if len(data) > config.MAX_FILE_SIZE:
            raise HTTPException(
                status_code=413,
                detail="Image exceeds the 5 MB size limit",
            )
        try:
            # 5. Decode and validate image
            image_bgr = self.image_processor.decode(data)
            # 6. Convert BGR to RGB
            image_rgb = cv.cvtColor(
                image_bgr,
                cv.COLOR_BGR2RGB,
            )
            pil_image = Image.fromarray(image_rgb)
            # 7. Run ML prediction
            prediction_result = self.predictor.predict(pil_image)
            # 8. Extract model information
            model_info = prediction_result["modelInfo"]
            # 9. Build prediction section
            prediction_data = {
                "crop": prediction_result["crop"],
                "disease": prediction_result["disease"],
                "confidenceScore": prediction_result["confidenceScore"],
                "topPredictions": prediction_result["topPredictions"],
            }
            # 10. Build final response
            response_data = {
                "predictionStatus": "SUCCESS",
                "imageAssessment": {
                    "isSuitable": True,
                    "qualityStatus": "ACCEPTABLE",
                    "issues": [],
                },
                "prediction": prediction_data,
                "modelInfo": model_info,
            }
            return JSONResponse(
                status_code=200,
                content=response_data,
            )
        except ValueError as exc:
            raise HTTPException(
                status_code=400,
                detail=str(exc),
            ) from exc
    async def health(
        self,
        _: None = Depends(_validate_service_key),
    ) -> dict[str,str]:
        return {
            "service": "LeafDoc ML Service",
            "status": "Connected",
            "authentication": "Valid",
        }
    async def get(self) -> dict:
        return {
            "msg": "Hello from ml service.",
            "status": "Connected",
            "authorization": "unknown",
        }
    def start(self) -> None:
        uvicorn.run(
            self.app,
            host=config.HOST,
            port=config.PORT,
            log_level=config.LOG_LEVEL,
        )
