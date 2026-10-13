
import uvicorn
from fastapi import FastAPI, HTTPException, Request, Response, UploadFile, File, Header, Depends
from fastapi.responses import JSONResponse

from imageProcessor import ImageProcessor
import config

class Server:
    def __init__(self):
        self.app = FastAPI(title= config.APP_TITLE )
        self.image_processor = ImageProcessor()
        self._configure_routes()        
        self.app.add_exception_handler(Exception, self._defaultExceptionHandler)
        #self.app.add_middleware()

        # @self.app.middleware("http") #DEBUG - shows the incomming request in detail
        # async def log_request(request: Request, call_next):
        #     print("\n========== INCOMING REQUEST ==========")
        #     print("Method:", request.method)
        #     print("URL:", request.url)
        #     print("Path:", request.url.path)
        #     print("Headers:", dict(request.headers))

        #     response = await call_next(request)

        #     print("Response status:", response.status_code)
        #     print("======================================\n")

        #     return response

        
    def _configure_routes(self):
        self.app.add_api_route("/ml/predict", self.predict , methods=["POST"])
        self.app.add_api_route("/ml", self.get , methods=["GET"])
        self.app.add_api_route("/ml/health", self.health, methods=["GET","POST","OPTIONS"])
        
    @staticmethod
    def _defaultExceptionHandler(request:Request , exc: Exception) -> Response:
            return JSONResponse(
                status_code=500,
                content={
                     "detail": "Something went wrong",
                     "type": type(exc),
                     "msg": str(exc)
                    }
            )
    @staticmethod
    def _validate_service_key( x_ml_service_key: str = Header(...) ):
        if x_ml_service_key != config.SERVICE_KEY :
            raise HTTPException(status_code=403, detail="Error: Unauthorised access!")


    #ENDPOINTS 

    async def predict(self,
                file: UploadFile = File(...),
                _: None = Depends(_validate_service_key)
        ) -> Response: 
        
        data : bytes = await file.read()

        if not data:
            raise HTTPException(
                status_code=400,
                detail="Uploaded file is empty"
            )
        try:
            image = self.image_processor.decode(data)
        except ValueError as exc:
            raise HTTPException(
                status_code=400,
                detail=str(exc)
            ) from exc
        finally:
            await file.close()
        
        # receive image
        # call predictor
        # return prediction (json responce)
        return JSONResponse(
            status_code=200,
            content={
                "filename": file.filename,
                "size": len(data),
                "width": int(image.shape[1]),
                "height": int(image.shape[0]),
                "message": "Image decoded successfully"
            }
        )

    async def health(self, _: None = Depends(_validate_service_key))  -> dict[str,str]:
        return {
            "service": "ML Service",
            "status": "Connected",
            "authentication": "Valid"
        }
    
    async def get(self) -> dict[str,str]:
        return {"msg" : "Hello from ml service. ", "status":"Connected", "authorization" : "unknown" }


    def start(self) -> None:
        uvicorn.run( self.app,
                     host=config.HOST,
                     port=config.PORT,
                     log_level=config.LOG_LEVEL,
                )
