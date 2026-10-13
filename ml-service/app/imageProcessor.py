import cv2 as cv
import numpy as np
from cv2.typing import MatLike

class ImageProcessor:
    def __init__(self) -> None:
            self.MAX_WIDTH = 4000
            self.MAX_HEIGHT = 4000
            self.MAX_PIXELS = 12_000_000

    def decode(self, data: bytes) -> MatLike:
        if not data:
            raise ValueError("Image is empty")

        encoded = np.frombuffer(data, dtype=np.uint8)

        image : MatLike | None = cv.imdecode(encoded, cv.IMREAD_COLOR)

        if image is None:
            raise ValueError("Invalid or unsupported image")

        height, width = image.shape[:2]

        if (
            width > self.MAX_WIDTH
            or height > self.MAX_HEIGHT
            or width * height > self.MAX_PIXELS
        ):
            raise ValueError("Image dimensions exceed limits")
        cv.typing.MatLike
        return image