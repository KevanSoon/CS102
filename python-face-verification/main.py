from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from deepface import DeepFace
import tempfile
import os
import time
from typing import Dict

app = FastAPI(title="Face Verification API", version="1.0.0")

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Adjust this in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
async def root():
    return {"message": "Face Verification API is running"}


@app.post("/api/face_match")
async def face_match(
    image1: UploadFile = File(..., description="First image for comparison"),
    image2: UploadFile = File(..., description="Second image for comparison")
):
    """
    Compare two face images and return verification result and confidence score.
    
    Parameters:
    - image1: First image file (uploaded image)
    - image2: Second image file (student image)
    
    Returns:
    - Dictionary containing verified (boolean) and confidence score (0-100)
    """
    temp_file1 = None
    temp_file2 = None
    
    try:
        start_time = time.time()
        
        # Create temporary files for the uploaded images
        temp_file1 = tempfile.NamedTemporaryFile(delete=False, suffix=".jpg")
        temp_file2 = tempfile.NamedTemporaryFile(delete=False, suffix=".jpg")
        
        # Write uploaded files to temp files
        content1 = await image1.read()
        content2 = await image2.read()
        
        temp_file1.write(content1)
        temp_file2.write(content2)
        temp_file1.close()
        temp_file2.close()
        
        print(f"Processing face verification...")
        print(f"Image 1: {temp_file1.name} ({len(content1)} bytes)")
        print(f"Image 2: {temp_file2.name} ({len(content2)} bytes)")
        
        # Perform face verification using DeepFace
        result = DeepFace.verify(
            img1_path=temp_file1.name,
            img2_path=temp_file2.name,
            model_name="Facenet",
            detector_backend="opencv",
            distance_metric="cosine",
            enforce_detection=False  # Don't fail if face is not detected
        )
        
        print(f"DeepFace raw result: {result}")
        
        # Extract only verified and confidence from DeepFace result
        verified = result.get("verified", False)
        confidence = result.get("confidence",0.00)
        
        confidence = round(confidence, 2)
        
        print(f"Verified: {verified}")
        print(f"Confidence: {confidence}%")
        print("=" * 60)
        print(f"RETURNING: verified={verified}, confidence={confidence}")
        print("=" * 60)
        
        # Return only verified and confidence
        return {"verified": verified, "confidence": confidence}
        
    except ValueError as e:
        # Handle face detection failures
        print(f"ValueError during face verification: {str(e)}")
        raise HTTPException(
            status_code=400,
            detail=f"Face detection failed: {str(e)}"
        )
    except Exception as e:
        print(f"Exception during face verification: {type(e).__name__}: {str(e)}")
        import traceback
        traceback.print_exc()
        raise HTTPException(
            status_code=500,
            detail=f"Error processing images: {str(e)}"
        )
    finally:
        # Clean up temporary files
        try:
            if temp_file1 and os.path.exists(temp_file1.name):
                os.unlink(temp_file1.name)
            if temp_file2 and os.path.exists(temp_file2.name):
                os.unlink(temp_file2.name)
        except Exception as e:
            print(f"Error cleaning up temp files: {e}")


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "face-verification"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
