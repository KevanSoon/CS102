# CS102 Smart Attendance Project

A comprehensive smart attendance tracking system that combines **face recognition technology** with traditional attendance management. This system provides both automated face-based attendance marking and manual attendance tracking capabilities for educational institutions.

## 📋 Project Description

The CS102 Smart Attendance Project is a full-stack application designed to modernize attendance tracking in educational settings. The system leverages **computer vision** and **machine learning** to automatically identify students through facial recognition, while also supporting manual attendance marking for flexibility.

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

### System Architecture:
The application follows a **three-tier architecture** with clear separation between:
- **Backend API**: Spring Boot REST services handling business logic
- **Frontend Interface**: Modern web application with real-time face detection
- **Face Recognition Service**: FastAPI-based DeepFace service for face verification
- **Database Layer**: Supabase (PostgreSQL) with cloud storage

## 🛠️ Tech Stack

### Backend Technologies
- **Java 17**: Core programming language
- **Spring Boot 3.5.5**: Main application framework
- **Spring Security**: Authentication and authorization
- **Spring Web**: REST API development
- **Spring WebFlux**: Reactive HTTP client for Supabase integration
- **Spring Boot Actuator**: Application monitoring and health checks
- **Spring Scheduling**: Automated task scheduling
- **Maven**: Dependency management and build tool
- **JWT (jjwt)**: Token-based authentication
- **OpenCSV**: CSV export functionality
- **Apache PDFBox**: PDF report generation

### Frontend Technologies
- **HTML5/CSS3**: Modern web interface
- **JavaScript (ES6+)**: Client-side functionality
- **Vite**: Frontend build tool and development server
- **Hugging Face Transformers**: Client-side face detection model (Xenova/gelan-c_all)


### AI & Machine Learning
- **DeepFace**: Face verification using Facenet model
- **FastAPI**: External face recognition service
- **Computer Vision**: Real-time face detection and recognition
- **Confidence Scoring**: ML-based attendance verification

### Database & Infrastructure
- **Supabase**: PostgreSQL database with authentication and storage
- **Supabase Storage**: Cloud storage for student face images
- **UUID**: Unique identifier generation
- **Database Indexing**: Optimized query performance
- **Foreign Key Constraints**: Data integrity

### External Services & APIs
- **Supabase Auth**: User authentication and JWT token management
- **Supabase Storage API**: Image upload and retrieval
- **FastAPI Face Recognition Service**: External ML service for face verification
- **RESTful API**: Complete API for frontend integration
- **CORS Configuration**: Cross-origin resource sharing
- **Multipart File Upload**: Image processing capabilities

### Development & Testing
- **JUnit**: Unit testing framework
- **Spring Boot Test**: Integration testing
- **Maven Wrapper**: Consistent build environment
- **Environment Configuration**: Externalized configuration management

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

### Application Configuration

The application uses `application.yml` for configuration with the following key settings:

- **Face Recognition Service**: DeepFace service URL (default: `http://localhost:8000/api/face_match`)
- **CORS Origins**: Configured for frontend integration
- **Actuator Endpoints**: Health, info, metrics, and Prometheus monitoring
- **Graceful Shutdown**: Enabled for clean application termination

## 🔌 API Endpoints

### Authentication APIs (Public)
- **POST** `/api/auth/signup` - Register a new student or professor
- **POST** `/api/auth/signin` - Sign in with email and password
- **POST** `/api/auth/signout` - Sign out current user (requires Bearer token)
- **GET** `/api/auth/user` - Get current user information (requires Bearer token)
- **POST** `/api/auth/refresh` - Refresh access token
- **POST** `/api/auth/resend-verification` - Resend email verification
- **PUT** `/api/auth/update-password` - Update user password (requires Bearer token)

### Attendance Record APIs
- **POST** `/api/attendance_records` - Create a new attendance record
- **GET** `/api/attendance_records` - Get all attendance records
  - Query params: `session_id`, `student_id` (optional filters)
- **PATCH** `/api/attendance_records/{id}` - Update attendance record

### Face Data APIs
- **POST** `/api/face_data` - Create face data record
- **POST** `/api/face_data/upload` - Upload student face image to Supabase Storage
  - Form data: `image` (file), `studentId` (string)

### Face Recognition APIs
- **POST** `/api/face-batch/face-compare-all` - Compare uploaded face with all student faces
  - Form data: `image` (file)
  - Returns: Best match student ID, confidence score, and auto-marks attendance

### Session Management APIs
- **POST** `/api/sessions` - Create new attendance session
- **GET** `/api/sessions` - Get all sessions
- **GET** `/api/sessions/active/{profId}` - Get active session for a professor
- **GET** `/api/sessions/{sessionId}/students` - Get students enrolled in a session
- **PATCH** `/api/sessions/{id}/close` - Close a session (marks absent students automatically)

### Student Management APIs
- **GET** `/api/students` - Get all students
- **GET** `/api/students/{id}` - Get student details
- **PATCH** `/api/students/{id}` - Update student information

### Professor Management APIs
- **GET** `/api/professors/{id}/classes` - Get classes taught by a professor

### Groups Management APIs
- **GET** `/api/groups` - Get all groups
- **PATCH** `/api/groups/{classCode}/{groupNumber}` - Update group student list

### Classes Management APIs
- **GET** `/api/classes` - Get all classes

### Reporting APIs
- **GET** `/api/reports/summary` - Get attendance summary report
  - Query params: `className` (optional filter)
- **GET/POST** `/api/reports/generate` - Generate attendance report (CSV or PDF)
  - Query params: `format` (csv/pdf), `className`, `startDate`, `endDate`

### Health & Monitoring APIs
- **GET** `/actuator/health` - Application health status
- **GET** `/actuator/info` - Application information
- **GET** `/actuator/metrics` - Detailed application metrics
- **GET** `/actuator/prometheus` - Prometheus-compatible metrics

## 🚀 Running the Application

### Prerequisites
- Java 17 or higher
- Maven 3.6+ (or use Maven Wrapper)
- Node.js 16+ and npm
- Python 3.8+ (for face recognition service)
- Supabase account and project

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

**API Documentation:**
- Interactive API docs: `http://localhost:8000/docs`
- Alternative docs: `http://localhost:8000/redoc`

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

### Complete Setup Process

1. **Set up Supabase**:
   - Create a Supabase project
   - Set up authentication
   - Create a storage bucket named `student-images` (public)
   - Configure environment variables

2. **Start Face Recognition Service**: Run the Python FastAPI service on port 8000

3. **Start Backend**: Run the Spring Boot application on port 8080

4. **Start Frontend**: Run the Vite development server on port 5173

5. **Access Application**: Navigate to `http://localhost:5173`

6. **Register Users**: Create accounts and upload face data

7. **Create Sessions**: Set up attendance sessions

8. **Test Face Recognition**: Use the camera interface for attendance marking

## ✨ Key Features & Capabilities

### 🎯 Face Recognition System
- **Real-time Detection**: Live camera feed with instant face detection using Hugging Face Transformers
- **Student Identification**: Automatic student recognition using DeepFace verification
- **Batch Comparison**: Compare uploaded face with all registered student faces
- **Confidence Scoring**: ML-based confidence levels for attendance verification
- **Auto-attendance Marking**: Automatically marks attendance when face match is verified

### 📊 Attendance Management
- **Dual Methods**: Support for both automatic (AI) and manual attendance marking
- **Session Management**: Create and manage attendance sessions with time constraints
- **Status Tracking**: Track present, absent, and late attendance statuses
- **Historical Records**: Complete attendance history with timestamps
- **Auto-absent Marking**: Automatically marks students as absent when session is closed

### 🔐 Security & Authentication
- **Supabase Auth**: Built-in authentication with JWT tokens
- **Role-based Access Control**:
  - **ROLE_STUDENT**: Access to attendance, face data, and student endpoints
  - **ROLE_PROFESSOR**: Full access to sessions, professors, and all student features
- **JWT Token Validation**: Automatic token verification on every request
- **Stateless Sessions**: No server-side session storage (JWT-based)
- **Secure API**: Protected REST endpoints with proper authentication
- **CORS Configuration**: Secure cross-origin resource sharing

### 📈 Analytics & Reporting
- **Attendance Reports**: Comprehensive reporting for sessions and students
- **Export Capabilities**: CSV and PDF export functionality
- **Summary Reports**: Attendance rate calculations per student
- **Date Range Filtering**: Filter reports by date range and class
- **Real-time Monitoring**: Live attendance tracking and status updates
- **Performance Metrics**: System performance monitoring and health checks

### 🌐 Modern Web Interface
- **Responsive Design**: Mobile-friendly interface for all devices
- **Real-time Updates**: Live data synchronization across the application
- **Intuitive UX**: User-friendly interface for easy navigation
- **Camera Integration**: Seamless webcam integration for face recognition
- **Student & Professor Views**: Separate interfaces for different user roles

### 🗄️ Database Management
- **Supabase PostgreSQL**: Robust relational database with ACID compliance
- **Cloud Storage**: Supabase Storage for face image storage
- **Data Integrity**: Foreign key constraints and unique constraints
- **Optimized Queries**: Database indexing for improved performance
- **UUID Primary Keys**: Globally unique identifiers

### ⏰ Automated Scheduling
- **Session Scheduler**: Automated task scheduling for session management
- **Auto-close Sessions**: Automatic session closure and absent marking

## 🔗 Application Monitoring

The application includes comprehensive monitoring capabilities:

### Health & Metrics
- **Application Health**: `GET /actuator/health` - Overall system health status
- **Application Info**: `GET /actuator/info` - Application metadata and version info
- **Metrics**: `GET /actuator/metrics` - Detailed application metrics
- **Prometheus Metrics**: `GET /actuator/prometheus` - Prometheus-compatible metrics

### Database Monitoring
- **Connection Health**: Database connectivity monitoring
- **Query Performance**: Database query performance tracking

## 🗄️ Database Schema

The application uses Supabase (PostgreSQL) with the following main entities:

### Core Tables
- **`students`**: Student information (id, name, email, code, phone)
- **`professors`**: Professor information (id, name)
- **`sessions`**: Attendance sessions (id, name, date, start_time, end_time, class_code, group_number, active)
- **`attendance_records`**: Attendance records (id, student_id, session_id, status, confidence, method, marked_at)
- **`face_data`**: Student face data for recognition (id, student_id, image_url)
- **`groups`**: Class groups (id, class_code, group_number, student_list)
- **`classes`**: Class information (id, class_code, name)
- **`auth.users`**: Managed by Supabase (id, email, encrypted_password, user_metadata)

### Key Relationships
- Students can have multiple attendance records
- Sessions can have multiple attendance records
- Students can have multiple face data entries
- Groups contain lists of student IDs
- Sessions are linked to classes and groups
- Unique constraints prevent duplicate attendance records

### Database Features
- **UUID Primary Keys**: Globally unique identifiers
- **Foreign Key Constraints**: Data integrity enforcement
- **Indexes**: Optimized query performance
- **Timestamps**: Automatic creation and update tracking
- **JSON Arrays**: Student lists stored as JSON arrays in groups

## 📁 Project Structure

```
smart-attendance-project/
├── src/main/java/com/cs102/attendance/
│   ├── config/                 # Configuration classes
│   │   ├── CorsConfig.java
│   │   ├── CorsProperties.java
│   │   ├── SecurityConfig.java
│   │   └── SupabaseConfig.java
│   ├── controller/             # REST API controllers
│   │   ├── AttendanceRecordController.java
│   │   ├── AuthController.java
│   │   ├── ClassesController.java
│   │   ├── FaceBatchCompareController.java
│   │   ├── FaceDataController.java
│   │   ├── GroupsController.java
│   │   ├── ProfessorController.java
│   │   ├── ReportController.java
│   │   ├── SessionController.java
│   │   └── StudentController.java
│   ├── dto/                    # Data Transfer Objects
│   │   ├── AttendanceRecordUpdateDTO.java
│   │   ├── AuthResponse.java
│   │   ├── FaceDataUpdateDTO.java
│   │   ├── FaceVerificationResult.java
│   │   ├── GroupUpdateDTO.java
│   │   ├── ProfessorUpdateDTO.java
│   │   ├── SessionUpdateDTO.java
│   │   ├── SignInRequest.java
│   │   ├── SignUpRequest.java
│   │   ├── StudentUpdateDTO.java
│   │   └── SupabaseSignUpRequest.java
│   ├── model/                  # Data models
│   │   ├── AttendanceRecord.java
│   │   ├── Classes.java
│   │   ├── FaceData.java
│   │   ├── Groups.java
│   │   ├── Professor.java
│   │   ├── Session.java
│   │   ├── Student.java
│   │   └── User.java
│   ├── scheduler/              # Scheduled tasks
│   │   └── SessionScheduler.java
│   ├── security/               # Security configuration
│   │   └── JwtAuthenticationFilter.java
│   ├── service/                # Business logic services
│   │   ├── AttendanceRecordService.java
│   │   ├── ClassesService.java
│   │   ├── FaceCompareService.java
│   │   ├── FaceDataService.java
│   │   ├── GroupsService.java
│   │   ├── ProfessorService.java
│   │   ├── SessionService.java
│   │   ├── StudentService.java
│   │   ├── SupabaseAuthService.java
│   │   └── SupabaseService.java
│   └── Cs102AttendanceProjectApplication.java
├── src/main/resources/
│   ├── application.yml         # Application configuration
│   └── fonts/
│       └── NotoSans-Regular.ttf
├── video-object-detection/     # Frontend application
│   ├── src/
│   │   ├── auth.js
│   │   ├── authCheck.js
│   │   ├── face-detection-model.js
│   │   ├── index.js
│   │   ├── professor.js
│   │   ├── sessionState.js
│   │   └── student.js
│   ├── index.html
│   ├── professor.html
│   ├── student.html
│   ├── styles.css
│   ├── package.json
│   └── vite.config.js
├── python-face-verification/  # Face recognition service
│   ├── main.py
│   ├── requirements.txt
│   └── README.md
├── pom.xml                     # Maven configuration
├── mvnw                        # Maven wrapper (Unix)
├── mvnw.cmd                    # Maven wrapper (Windows)
└── README.md                   # This file
```

## 🧪 Testing

### Run Unit Tests
```bash
# Using Maven Wrapper
./mvnw test

# Using Maven
mvn test
```

### Run Integration Tests
```bash
# Using Maven Wrapper
./mvnw verify

# Using Maven
mvn verify
```
