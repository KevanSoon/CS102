# CS102 Smart Attendance Project

A comprehensive smart attendance tracking system that combines **face recognition technology** with traditional attendance management. This system provides both automated face-based attendance marking and manual attendance tracking capabilities for educational institutions.

## 📋 Project Description

A modern, AI-powered attendance tracking system combining **facial recognition**, **real-time monitoring**, and **manual overrides** for educational institutions. Built with **Java Spring Boot**, **Vite/JavaScript**, **Supabase**, and **FastAPI (DeepFace)**.

### Key Features:
- **🎯 Automated Face Recognition**: Real-time face detection and student identification using DeepFace
- **📱 Web-based Interface**: Modern, responsive web interface for both students and professors
- **🔐 Secure Authentication**: Supabase authentication with JWT-based authorization
- **📊 Attendance Analytics**: Comprehensive reporting with CSV and PDF export
- **🔄 Dual Attendance Methods**: Support for both automatic (face recognition) and manual attendance marking
- **📈 Real-time Monitoring**: Live attendance tracking with confidence scoring
- **🗄️ Database Management**: Supabase (PostgreSQL) with cloud storage for face images
- **🌐 RESTful API**: Complete REST API for frontend integration
- **⏰ Session Management**: Automated session scheduling and absent student marking

## 🛠️ Tech Stack

**Backend:** Java 17, Spring Boot 3, **Supabase Auth (JWT Validation)**
**Frontend:** HTML/CSS, JavaScript (ES6+), Vite  
**AI/ML:** DeepFace (Facenet), Hugging Face Transformers  
**Database:** Supabase PostgreSQL + Supabase Storage  
**Face Service:** Python FastAPI + Uvicorn  
**Build Tools:** Maven, npm  

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the project root or set the following environment variables:

```env
# Supabase Configuration
supabase.url=https://your-project-id.supabase.co
supabase.api-key=your-anon-public-key
supabase.service-role-key=your-service-role-key
supabase.jwt.secret=your-jwt-secret

# Face Recognition Service
face-service.deepface-url=http://localhost:8000/api/face_match

# CORS Configuration (comma-separated)
CORS_ALLOWED_ORIGINS=https://cs-102-vert.vercel.app,http://localhost:5173,http://localhost:5174,http://localhost:8080
```

### Running the Backend

#### Using Maven Wrapper (Recommended)
```bash
# On Unix/Linux/macOS
./mvnw spring-boot:run

# On Windows
mvnw.cmd spring-boot:run
```

#### Using Maven
```bash
# Clean and install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

#### Using JAR file
```bash
# Build the JAR file
mvn clean package

# Run the JAR file
java -jar target/attendance-0.0.1-SNAPSHOT.jar
```

The backend will start on `http://localhost:8080` by default.

### Running the Face Recognition Service

The Python FastAPI service provides face verification capabilities:

```bash
# Navigate to Python service directory
cd python-face-verification

# Activate virtual environment (if using one)
# On Unix/Linux/macOS:
source venv/bin/activate
# On Windows:
venv\Scripts\activate

# Install dependencies (if not already installed)
pip install -r requirements.txt

# Start the FastAPI server
python main.py

# Or use uvicorn directly:
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

The face recognition service will be available at `http://localhost:8000`.

### Running the Frontend

The frontend application is located in the `video-object-detection` directory:

```bash
# Navigate to frontend directory
cd video-object-detection

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will be available at `http://localhost:5173` by default.

## 📁 Project Structure

```
smart-attendance-project/
├── src/main/java/com/cs102/attendance/
├── video-object-detection/     # Frontend application
├── python-face-verification/  # Face recognition service
└── README.md                   # This file
```
