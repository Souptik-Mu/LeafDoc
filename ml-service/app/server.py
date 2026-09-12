
import uvicorn
from fastapi import FastAPI, HTTPException, Request, Response, UploadFile, File, Header
from fastapi.responses import JSONResponse

import config

class Server:
    def __init__(self):
        self.app = FastAPI(title= config.APP_TITLE )
        self._configure_routes()        
        self.app.add_exception_handler(Exception, self._defaultExceptionHandler)
        
    def _configure_routes(self):
        self.app.add_api_route("/ml/predict", self.predict , methods=["POST"])
        self.app.add_api_route("/ml", self.get , methods=["GET"])

        
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

    def _validate_service_key(self, x_ml_service_key: str = Header(...) ):
        if x_ml_service_key != config.SERVICE_KEY :
            raise HTTPException(status_code=403)
       

    #ENDPOINTS 

    async def predict(self, file: UploadFile = File(...) ) -> Response: #_: None = Depends(self._validate_service_key)
        data : bytes = await file.read()
        # receive image
        # call predictor
        # return prediction (json responce)
        return JSONResponse(
            status_code=200,
            content={
                "filename": file.filename,
                "size": len(data)
            }
        )
    
    async def get(self):
         return {"msg" : "Helloooids"}


    def start(self) -> None:
        uvicorn.run( self.app,
                     host=config.HOST,
                     port=config.PORT,
                     log_level=config.LOG_LEVEL,
                )
