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
from dto.types import PredictionItemDTO , PredictionResponseDTO, ModelInfoDTO
class Server:
    def __init__(self):
        self.app = FastAPI(title=config.APP_TITLE)
        self.image_processor = ImageProcessor()
        self.model = PlantDiseaseModel()
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
        # Validate file type
        if file.content_type not in config.ALLOWED_TYPES:
            await file.close()
            raise HTTPException(
                status_code=400,
                detail="Only JPEG and PNG images are allowed",
            )
        # Read uploaded image
        data = await file.read()
        await file.close()
        # Validate empty file
        if not data:
            raise HTTPException(
                status_code=400,
                detail="Uploaded file is empty",
            )
        # Validate file size
        if len(data) > config.MAX_FILE_SIZE:
            raise HTTPException(
                status_code=413,
                detail="Image exceeds the 5 MB size limit",
            )
        try:
            #Decode and validate image
            image_bgr = self.image_processor.decode(data)
            image_rgb = cv.cvtColor(
                image_bgr,
                cv.COLOR_BGR2RGB,
            )
            pil_image = Image.fromarray(image_rgb)
            # Run ML prediction
            prediction :list[PredictionItemDTO] = self.model.predict(pil_image)
            model_info = ModelInfoDTO(
                modelName=self.model.MODEL_NAME,
                modelVersion=self.model.MODEL_VERSION,
                inputSize=self.model.INPUT_SIZE,
            )

            return JSONResponse(
                status_code=200,
                content=PredictionResponseDTO(
                    predictionStatus="SUCCESS",
                    prediction=prediction,
                    modelInfo=model_info,
                ),
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
    
    async def get(self) -> dict[str,str]:
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
