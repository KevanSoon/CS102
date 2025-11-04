# Debugging Guide - "Failed to verify" Issue

## Problem
The JavaScript shows "Failed to verify" very quickly with no logs appearing in Java or FastAPI.

## Added Logging

### JavaScript (face-detection-model.js)
✅ Added console logging for:
- Image blob creation
- File size before sending
- POST request URL
- Response status and body
- Parsed response values
- Verification success/failure

### Java Backend (FaceBatchCompareController.java)
✅ Added System.out.println for:
- Request received notification
- Image filename, size, content type
- Number of students fetched from database
- Each student comparison attempt
- Similarity scores
- Best match updates
- Final response before returning

### Java Backend (FaceCompareController.java)
✅ Added System.out.println for:
- Request received notification
- Image URLs and Base64 length
- Temp file creation
- FaceCompareService call
- Comparison result

### Java Backend (FaceCompareService.java)
Already has:
- DeepFace API response logging
- Error logging

## How to Debug

### Step 1: Check Browser Console
Open your browser's Developer Tools (F12) and go to the Console tab.

**What to look for:**
```
Sending image to API, file size: XXXXX bytes
Sending POST request to: http://localhost:8080/api/face-batch/face-compare-all
Response status: 200 OK
API Response: {...}
Verification response received: {...}
```

**Common Issues:**
- ❌ **CORS Error** - "Access to fetch... has been blocked by CORS policy"
  - Solution: Make sure Java backend is running and CORS is configured
  
- ❌ **Network Error** - "Failed to fetch" or "net::ERR_CONNECTION_REFUSED"
  - Solution: Java backend is not running on port 8080
  
- ❌ **400/500 Error** - "Response status: 400/500"
  - Solution: Check Java console logs for error details

### Step 2: Check Java Console
Look at the terminal where you ran `mvn spring-boot:run`

**What to look for:**
```
=== Face Batch Compare Request Received ===
Image filename: detected.png
Image size: XXXXX bytes
Content type: image/png
Base64 image length: XXXXX
Fetched X students from database
Comparing with student: STUDENT_ID
=== Face Compare URL Request Received ===
Image URL 2: https://...
Calling faceCompareService...
DeepFace API Response: {...}
Student STUDENT_ID similarity: 0.XX
Final response: {...}
=== Face Batch Compare Request Complete ===
```

**Common Issues:**
- ❌ **Nothing printed** - Request never reached Java backend
  - Check if Spring Boot is running
  - Check URL in JavaScript matches `http://localhost:8080`
  - Check CORS configuration
  
- ❌ **Error in face comparison** - Exception logged
  - Check if FastAPI is running on port 8000
  - Check `application.yml` has correct `deepface-url`
  
- ❌ **No students fetched** - "Fetched 0 students from database"
  - Database has no student records
  - Check database connection

### Step 3: Check FastAPI Console
Look at the terminal where you ran the FastAPI service

**What to look for:**
```
INFO:     127.0.0.1:XXXXX - "POST /api/face_match HTTP/1.1" 200 OK
```

**Common Issues:**
- ❌ **Nothing printed** - FastAPI not receiving requests
  - FastAPI might not be running
  - Check if running on port 8000: `http://localhost:8000/health`
  
- ❌ **400 Error** - Face detection failed
  - Images don't contain clear faces
  - Image quality too poor

### Step 4: Test Individual Components

#### Test 1: Check if Java backend is running
```powershell
curl http://localhost:8080/actuator/health
```
Should return: `{"status":"UP"}`

#### Test 2: Check if FastAPI is running
```powershell
curl http://localhost:8000/health
```
Should return: `{"status":"healthy","service":"face-verification"}`

#### Test 3: Test FastAPI directly
```powershell
# Create two test images first, then:
curl -X POST "http://localhost:8000/api/face_match" `
  -F "image1=@path/to/test1.jpg" `
  -F "image2=@path/to/test2.jpg"
```

## Most Likely Issues

Based on "Failed to verify" appearing quickly with no logs:

### 1. ❌ Java Backend Not Running
**Symptom:** Browser console shows "Failed to fetch" or "net::ERR_CONNECTION_REFUSED"
**Solution:** 
```powershell
mvn spring-boot:run
```

### 2. ❌ CORS Blocking Request
**Symptom:** Browser console shows CORS error
**Solution:** Check that your frontend origin is in `application.yml` cors.allowed-origins

### 3. ❌ FastAPI Not Running
**Symptom:** Java logs show "Error in faceCompare: Connection refused"
**Solution:**
```powershell
cd python-face-verification
.\start.ps1
```

### 4. ❌ Empty Response from Backend
**Symptom:** Browser console shows `API Response: {}` or `highestSimilarity: undefined`
**Solution:** Check Java logs to see if students were fetched from database

## Quick Checklist

Run through this checklist:
- [ ] Java backend is running (`mvn spring-boot:run`)
- [ ] FastAPI is running (`cd python-face-verification && .\start.ps1`)
- [ ] Browser console is open (F12)
- [ ] Java console is visible
- [ ] FastAPI console is visible
- [ ] Try the face detection again
- [ ] Check browser console for errors
- [ ] Check Java console for logs
- [ ] Check FastAPI console for requests

## Expected Flow

1. **JavaScript captures image** → Console: "Sending image to API..."
2. **POST to Java backend** → Console: "Sending POST request..."
3. **Java receives request** → Java logs: "=== Face Batch Compare Request Received ==="
4. **Java calls FaceCompareService** → Java logs: "Calling faceCompareService..."
5. **Java calls FastAPI** → FastAPI logs: "POST /api/face_match"
6. **FastAPI returns result** → Java logs: "DeepFace API Response: {...}"
7. **Java returns to frontend** → Console: "API Response: {...}"
8. **JavaScript updates UI** → Green box or "Failed to verify"

## Next Steps

After adding all this logging, try again and tell me:
1. What appears in the **browser console**?
2. What appears in the **Java console**?
3. What appears in the **FastAPI console**?

This will help identify exactly where the issue is occurring!
