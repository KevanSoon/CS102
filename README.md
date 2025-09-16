# CS102 Smart Attendance Project

A backend Spring Boot application for a smart attendance tracking system.

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the project root or set the following environment variables:

```env
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-1-us-east-2.pooler.supabase.com:6543/postgres
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password


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

### Accessing the Application

When you run the application locally, you'll be redirected to a login page due to Spring Security being enabled.

**Login Credentials:**
- **Username:** `user`
- **Password:** A new password is generated every time you run the project - check your terminal/console output

The generated password will appear in the logs like this:
```
Using generated security password: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

Copy this password from your terminal to log into the application.

## 🔗 Application Monitoring

The application includes Spring Boot Actuator for basic monitoring:

- **Application Health**: `GET /actuator/health`
- **Application Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus Metrics**: `GET /actuator/prometheus`

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

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add some amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request