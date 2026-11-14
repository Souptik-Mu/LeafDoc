
from typing import Literal
from pydantic import BaseModel, Field

class PredictionItemDTO(BaseModel):
    class_label: str = Field(alias="classLabel")
    score: float = Field(alias="confidenceScore",ge=0.0, le=1.0)

class ModelInfoDTO(BaseModel):
    model_name: str = Field(alias="modelName")
    model_version: str = Field(alias="modelVersion")
    input_size: int = Field(alias="inputSize")

class PredictionResponseDTO(BaseModel):
    prediction_status: Literal["SUCCESS"] = Field(
        alias="predictionStatus"
    )
    prediction: list[PredictionItemDTO]
    model_info: ModelInfoDTO = Field(alias="modelInfo")
    model_config = {
        "populate_by_name": True,
    }
