// package com.cs102.attendance.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import javax.sql.DataSource;
// import java.sql.Connection;
// import java.sql.SQLException;
// import java.util.HashMap;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/health")
// public class HealthController {

//     @Autowired
//     private DataSource dataSource;

//     @GetMapping("/database")
//     public ResponseEntity<Map<String, Object>> checkDatabaseConnection() {
//         Map<String, Object> response = new HashMap<>();
        
//         try (Connection connection = dataSource.getConnection()) {
//             // Test the connection
//             boolean isValid = connection.isValid(5); // 5 second timeout
            
//             if (isValid) {
//                 response.put("status", "UP");
//                 response.put("database", "Connected");
//                 response.put("url", connection.getMetaData().getURL());
//                 response.put("driver", connection.getMetaData().getDriverName());
//                 response.put("version", connection.getMetaData().getDatabaseProductVersion());
//                 return ResponseEntity.ok(response);
//             } else {
//                 response.put("status", "DOWN");
//                 response.put("database", "Connection invalid");
//                 return ResponseEntity.status(503).body(response);
//             }
//         } catch (SQLException e) {
//             response.put("status", "DOWN");
//             response.put("database", "Connection failed");
//             response.put("error", e.getMessage());
//             return ResponseEntity.status(503).body(response);
//         }
//     }
// } 