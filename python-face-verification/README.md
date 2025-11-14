# Face Verification FastAPI Service

This is a FastAPI service that provides face verification capabilities using DeepFace.

## Setup

1. Create a virtual environment (recommended):
```powershell
python -m venv venv
.\venv\Scripts\Activate.ps1
```

2. Install dependencies:
```powershell
pip install -r requirements.txt
```

## Running the Service

Start the FastAPI server:
```powershell
python main.py
```

Or use uvicorn directly:
```powershell
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

The API will be available at: `http://localhost:8000`

## API Documentation

Once running, visit:
- Interactive API docs: `http://localhost:8000/docs`
- Alternative docs: `http://localhost:8000/redoc`

## Endpoints

### POST /api/face_match
Compare two face images and return verification results.

**Parameters:**
- `image1`: First image file (multipart/form-data)
- `image2`: Second image file (multipart/form-data)

**Response:**
```json
{
  "verified": true,
  "distance": 0.303235,
  "threshold": 0.68,
  "confidence": 90.92,
  "model": "Facenet",
  "detector_backend": "opencv",
  "similarity_metric": "cosine",
  "facial_areas": {
    "img1": {"x": 60, "y": 104, "w": 273, "h": 273},
    "img2": {"x": 259, "y": 96, "w": 252, "h": 252}
  },
  "time": 1.66
}
```

### GET /health
Health check endpoint.

## Configuration

The service uses:
- **Model**: Facenet
- **Detector Backend**: OpenCV
- **Distance Metric**: Cosine
- **Default Port**: 8000

## Integration with Java Backend

The Java backend should send POST requests to `http://localhost:8000/api/face_match` with two image files as multipart form data.
