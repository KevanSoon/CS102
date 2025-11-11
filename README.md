# CS102 Smart Attendance Project

A comprehensive smart attendance tracking system that combines **face recognition technology** with traditional attendance management. This system provides both automated face-based attendance marking and manual attendance tracking capabilities for educational institutions.

## 📋 Project Description

The CS102 Smart Attendance Project is a full-stack application designed to modernize attendance tracking in educational settings. The system leverages **computer vision** and **machine learning** to automatically identify students through facial recognition, while also supporting manual attendance marking for flexibility.

### Key Features:
- **🎯 Automated Face Recognition**: Real-time face detection and student identification using Hugging Face Transformers
- **📱 Web-based Interface**: Modern, responsive web interface for both students and professors
- **🔐 Secure Authentication**: Spring Security integration with role-based access control
- **📊 Attendance Analytics**: Comprehensive reporting and analytics dashboard
- **🔄 Dual Attendance Methods**: Support for both automatic (face recognition) and manual attendance marking
- **📈 Real-time Monitoring**: Live attendance tracking with confidence scoring
- **🗄️ Database Management**: PostgreSQL database
- **🌐 RESTful API**: Complete REST API for frontend integration and external systems

### System Architecture:
The application follows a **microservices-inspired architecture** with clear separation between:
- **Backend API**: Spring Boot REST services handling business logic
- **Frontend Interface**: Modern web application with real-time face detection
- **External AI Service**: FastAPI-based face recognition service
- **Database Layer**: PostgreSQL with proper relational modeling

## 🛠️ Tech Stack

### Backend Technologies
- **Java 17**: Core programming language
- **Spring Boot 3.5.5**: Main application framework
- **Spring Security**: Authentication and authorization
- **Spring Web**: REST API development
- **Spring Boot Actuator**: Application monitoring and health checks
- **Maven**: Dependency management and build tool

### Frontend Technologies
- **HTML5/CSS3**: Modern web interface
- **JavaScript (ES6+)**: Client-side functionality
- **Vite**: Frontend build tool and development server
- **Hugging Face Transformers**: Client-side face detection model
- **Gradio Client**: Integration with external AI services

### AI & Machine Learning
- **Hugging Face Transformers**: Face detection using Xenova/gelan-c_all model
- **FastAPI**: External face recognition service
- **Computer Vision**: Real-time face detection and recognition
- **Confidence Scoring**: ML-based attendance verification

### Database & Infrastructure
- **PostgreSQL**: Primary database
- **UUID**: Unique identifier generation
- **Database Indexing**: Optimized query performance
- **Foreign Key Constraints**: Data integrity

### External Services & APIs
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
supabase.key=your-anon-public-key
supabase.jwt.secret=your-jwt-secret

# Face Recognition Service
FACE_SERVICE_FASTAPI_URL=https://kevansoon-java-facerecognition-endpoint.hf.space/face-recognition

# Recognition Configuration
RECOGNITION_CONFIDENCE=0.70
RECOGNITION_COOLDOWN_MS=10000

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://cs-102-vert.vercel.app/,http://localhost:5173,http://localhost:8080
```

**Note**: See [SUPABASE_AUTH_SETUP.md](./SUPABASE_AUTH_SETUP.md) for detailed authentication setup and testing guide.

### Application Configuration

The application uses `application.yml` for configuration with the following key settings:

- **Face Recognition Confidence Threshold**: 0.70 (configurable)
- **Cooldown Period**: 10 seconds between face recognition attempts
- **CORS Origins**: Configured for frontend integration
- **Actuator Endpoints**: Health, info, metrics, and Prometheus monitoring

## 🔌 API Endpoints

### Authentication APIs (Public)
- **POST** `/api/auth/signup` - Register a new student or professor
- **POST** `/api/auth/signin` - Sign in with email and password
- **POST** `/api/auth/signout` - Sign out current user
- **GET** `/api/auth/user` - Get current user information
- **POST** `/api/auth/refresh` - Refresh access token

### Core Attendance APIs
- **POST** `/api/attendance/manual` - Mark attendance manually
- **POST** `/api/attendance/auto` - Mark attendance automatically with face recognition
- **GET** `/api/attendance/session/{sessionId}` - Get attendance records for a session
- **GET** `/api/attendance/student/{studentId}` - Get attendance history for a student

### Face Recognition APIs
- **POST** `/api/face-recognition/call` - Process face recognition with image upload
- **POST** `/api/face-data/upload` - Upload student face data for training
- **GET** `/api/face-data/student/{studentId}` - Get face data for a student

### Student Management APIs
- **GET** `/api/students` - Get all students
- **POST** `/api/students` - Create new student
- **GET** `/api/students/{studentId}` - Get student details
- **PATCH** `/api/students/{studentId}` - Update student information
- **DELETE** `/api/students/{studentId}` - Delete student

### Professor Management APIs (Professor Role Only)
- **GET** `/api/professors` - Get all professors
- **POST** `/api/professors` - Create new professor
- **GET** `/api/professors/{professorId}` - Get professor details
- **PATCH** `/api/professors/{professorId}` - Update professor information
- **DELETE** `/api/professors/{professorId}` - Delete professor

### Session Management APIs (Professor Role Only)
- **GET** `/api/sessions` - Get all sessions
- **POST** `/api/sessions` - Create new session
- **GET** `/api/sessions/{sessionId}` - Get session details
- **PUT** `/api/sessions/{sessionId}` - Update session information

### Reporting APIs
- **GET** `/api/reports/attendance-summary` - Get attendance summary report
- **GET** `/api/reports/session/{sessionId}` - Get detailed session report
- **GET** `/api/reports/student/{studentId}` - Get student attendance report

### Health & Monitoring APIs
- **GET** `/actuator/health` - Application health status
- **GET** `/actuator/info` - Application information
- **GET** `/actuator/metrics` - Application metrics
- **GET** `/actuator/prometheus` - Prometheus metrics

## 🚀 Running the Application

### Using Maven Wrapper (Recommended)

```bash
# On Unix/Linux/macOS
./mvnw spring-boot:run

# On Windows
mvnw.cmd spring-boot:run
```

### Using Maven

```bash
# Clean and install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

### Using JAR file

```bash
# Build the JAR file
mvn clean package

# Run the JAR file
java -jar target/attendance-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080` by default.

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

### Accessing the Application

The application uses **Supabase Authentication** with JWT tokens. To access the API:

1. **Sign Up**: Create an account using the `/api/auth/signup` endpoint
2. **Sign In**: Authenticate with `/api/auth/signin` to get a JWT token
3. **Use Token**: Include the token in the `Authorization` header for protected endpoints

**Example Authentication Flow:**

```bash
# 1. Sign up as a student
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "password123",
    "userMetadata": {
      "role": "student",
      "name": "John Doe"
    }
  }'

# 2. Sign in
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "password123"
  }'

# 3. Use the returned accessToken in subsequent requests
curl -X GET http://localhost:8080/api/students \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Complete Setup Process

1. **Start Backend**: Run the Spring Boot application
2. **Start Frontend**: Run the Vite development server
3. **Access Application**: Navigate to `http://localhost:5173`
4. **Register Users**: Create accounts and register face data
5. **Create Sessions**: Set up attendance sessions
6. **Test Face Recognition**: Use the camera interface for attendance marking

## ✨ Key Features & Capabilities

### 🎯 Face Recognition System
- **Real-time Detection**: Live camera feed with instant face detection
- **Student Identification**: Automatic student recognition using trained face data
- **Confidence Scoring**: ML-based confidence levels for attendance verification
- **Cooldown Protection**: Prevents duplicate attendance marking within configurable time periods

### 📊 Attendance Management
- **Dual Methods**: Support for both automatic (AI) and manual attendance marking
- **Session Management**: Create and manage attendance sessions with time constraints
- **Status Tracking**: Track present, absent, and late attendance statuses
- **Historical Records**: Complete attendance history with timestamps

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
- **Real-time Monitoring**: Live attendance tracking and status updates
- **Performance Metrics**: System performance monitoring and health checks

### 🌐 Modern Web Interface
- **Responsive Design**: Mobile-friendly interface for all devices
- **Real-time Updates**: Live data synchronization across the application
- **Intuitive UX**: User-friendly interface for easy navigation
- **Camera Integration**: Seamless webcam integration for face recognition

### 🗄️ Database Management
- **PostgreSQL**: Robust relational database with ACID compliance
- **Data Integrity**: Foreign key constraints and unique constraints
- **Optimized Queries**: Database indexing for improved performance

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

The application uses PostgreSQL with the following main entities:

### Core Tables
- **`students`**: Student information (ID, code, name, class, email, phone)
- **`professors`**: Professor information (ID, name)
- **`sessions`**: Attendance sessions (ID, name, date, start/end time)
- **`attendance_records`**: Attendance records (ID, student_id, session_id, status, confidence, method)
- **`face_data`**: Student face data for recognition (ID, student_id, image_url)
- **`auth.users`**: Managed by Supabase (email, encrypted_password, user_metadata)

### Key Relationships
- Students can have multiple attendance records
- Sessions can have multiple attendance records
- Students can have multiple face data entries
- Unique constraints prevent duplicate attendance records

### Database Features
- **UUID Primary Keys**: Globally unique identifiers
- **Foreign Key Constraints**: Data integrity enforcement
- **Indexes**: Optimized query performance
- **Timestamps**: Automatic creation and update tracking

## 📁 Project Structure

```
smart-attendance-project/
├── src/main/java/com/cs102/attendance/
│   ├── config/                 # Configuration classes
│   │   ├── CorsProperties.java
│   │   ├── RecognitionProperties.java
│   │   └── SecurityConfig.java
│   ├── controller/             # REST API controllers
│   │   ├── AttendanceController.java
│   │   ├── FaceDataController.java
│   │   ├── FastApiCallerController.java
│   │   ├── SessionController.java
│   │   └── StudentController.java
│   ├── entity/                 # JPA entities
│   │   ├── AttendanceRecord.java
│   │   ├── FaceData.java
│   │   ├── Session.java
│   │   └── Student.java
│   ├── service/                # Business logic services
│   │   ├── AttendanceService.java
│   │   ├── AutoMarker.java
│   │   ├── FaceDataService.java
│   │   ├── FastApiCallerService.java
│   │   └── ManualMarker.java
│   └── repository/             # Data access layer
│       ├── AttendanceRepository.java
│       ├── FaceDataRepository.java
│       └── StudentRepository.java
├── src/main/resources/
│   └── application.yml         # Application configuration
├── video-object-detection/    # Frontend application
│   ├── src/
│   │   ├── face-detection-model.js
│   │   ├── facedetection.js
│   │   └── student.js
│   ├── index.html
│   ├── student.html
│   └── professor.html
└── pom.xml                    # Maven configuration
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

## 🚀 Deployment & Production

### Building for Production

```bash
# Build the application
mvn clean package

# The JAR file will be created in target/ directory
java -jar target/attendance-0.0.1-SNAPSHOT.jar
```

### Frontend Production Build

```bash
cd video-object-detection
npm run build
# Built files will be in dist/ directory
```

### Environment Setup

For production deployment, ensure you have:

1. **PostgreSQL Database**: Set up with proper credentials
2. **Environment Variables**: Configure all required environment variables
3. **Face Recognition Service**: Ensure external FastAPI service is accessible
4. **CORS Configuration**: Update allowed origins for production domains
5. **Security Configuration**: Review and update security settings

### Docker Deployment (Optional)

```dockerfile
# Example Dockerfile for backend
FROM openjdk:17-jdk-slim
COPY target/attendance-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔧 Troubleshooting

### Common Issues

1. **Face Recognition Not Working**
   - Check camera permissions in browser
   - Verify FastAPI service is running and accessible
   - Check confidence threshold settings

2. **Database Connection Issues**
   - Verify PostgreSQL is running
   - Check database credentials in environment variables
   - Ensure database exists and migrations are applied

3. **CORS Errors**
   - Update CORS configuration in `application.yml`
   - Check frontend URL matches allowed origins

4. **Authentication Issues**
   - Check Spring Security configuration
   - Verify login credentials
   - Review security logs

## 📄 License

This project is part of the CS102 course at Singapore Management University.