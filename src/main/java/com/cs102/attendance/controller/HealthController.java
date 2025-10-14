// package com.cs102.attendance.controller;

// import com.zaxxer.hikari.HikariDataSource;
// import com.zaxxer.hikari.HikariPoolMXBean;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import javax.management.JMX;
// import javax.management.MBeanServer;
// import javax.management.ObjectName;
// import javax.sql.DataSource;
// import java.lang.management.ManagementFactory;
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

//     @GetMapping("/connection-pool")
//     public ResponseEntity<Map<String, Object>> checkConnectionPool() {
//         Map<String, Object> response = new HashMap<>();
        
//         try {
//             if (dataSource instanceof HikariDataSource) {
//                 HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                
//                 response.put("status", "UP");
//                 response.put("poolName", "AttendanceHikariCP");
                
//                 // Basic configuration details (always available)
//                 response.put("maximumPoolSize", hikariDataSource.getMaximumPoolSize());
//                 response.put("minimumIdle", hikariDataSource.getMinimumIdle());
//                 response.put("connectionTimeout", hikariDataSource.getConnectionTimeout());
//                 response.put("idleTimeout", hikariDataSource.getIdleTimeout());
//                 response.put("maxLifetime", hikariDataSource.getMaxLifetime());
//                 response.put("leakDetectionThreshold", hikariDataSource.getLeakDetectionThreshold());
                
//                 // Try to get HikariCP MBean for detailed metrics
//                 try {
//                     MBeanServer server = ManagementFactory.getPlatformMBeanServer();
//                     ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (AttendanceHikariCP)");
                    
//                     if (server.isRegistered(poolName)) {
//                         HikariPoolMXBean poolProxy = JMX.newMXBeanProxy(server, poolName, HikariPoolMXBean.class);
                        
//                         response.put("activeConnections", poolProxy.getActiveConnections());
//                         response.put("idleConnections", poolProxy.getIdleConnections());
//                         response.put("totalConnections", poolProxy.getTotalConnections());
//                         response.put("threadsAwaitingConnection", poolProxy.getThreadsAwaitingConnection());
                        
//                         // Health indicators
//                         int activeConnections = poolProxy.getActiveConnections();
//                         int totalConnections = poolProxy.getTotalConnections();
//                         int maxPoolSize = hikariDataSource.getMaximumPoolSize();
                        
//                         response.put("poolUtilization", String.format("%.2f%%", 
//                             (double) totalConnections / maxPoolSize * 100));
//                         response.put("activeUtilization", String.format("%.2f%%", 
//                             (double) activeConnections / maxPoolSize * 100));
                        
//                         // Warning thresholds
//                         if (totalConnections >= maxPoolSize * 0.9) {
//                             response.put("warning", "Connection pool is near maximum capacity");
//                         }
//                         if (poolProxy.getThreadsAwaitingConnection() > 0) {
//                             response.put("warning", "Threads are waiting for connections - possible bottleneck");
//                         }
//                     } else {
//                         response.put("mbeanStatus", "MBean not registered - pool metrics unavailable");
//                         response.put("note", "Pool configuration is available, but runtime metrics require MBean access");
//                     }
//                 } catch (Exception mbeanException) {
//                     response.put("mbeanError", "Cannot access pool MBean: " + mbeanException.getMessage());
//                     response.put("note", "Pool configuration is available, but runtime metrics are not accessible");
//                 }
                
//                 return ResponseEntity.ok(response);
//             } else {
//                 response.put("status", "UNKNOWN");
//                 response.put("message", "DataSource is not HikariCP");
//                 return ResponseEntity.ok(response);
//             }
//         } catch (Exception e) {
//             response.put("status", "ERROR");
//             response.put("error", "Failed to get connection pool info: " + e.getMessage());
//             return ResponseEntity.status(500).body(response);
//         }
//     }
    
//     @GetMapping("/connection-test")
//     public ResponseEntity<Map<String, Object>> testConnectionLeak() {
//         Map<String, Object> response = new HashMap<>();
        
//         try {
//             // Test multiple rapid connections to check for leaks
//             for (int i = 0; i < 3; i++) {
//                 try (Connection connection = dataSource.getConnection()) {
//                     connection.isValid(1);
//                 }
//             }
            
//             response.put("status", "UP");
//             response.put("message", "Connection leak test passed");
//             response.put("testConnections", 3);
//             return ResponseEntity.ok(response);
            
//         } catch (SQLException e) {
//             response.put("status", "DOWN");
//             response.put("error", "Connection leak test failed: " + e.getMessage());
//             return ResponseEntity.status(503).body(response);
//         }
//     }
    
//     @GetMapping("/pool-simple")
//     public ResponseEntity<Map<String, Object>> getSimplePoolInfo() {
//         Map<String, Object> response = new HashMap<>();
        
//         try {
//             if (dataSource instanceof HikariDataSource) {
//                 HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                
//                 response.put("status", "UP");
//                 response.put("dataSourceType", "HikariCP");
//                 response.put("poolName", hikariDataSource.getPoolName());
//                 response.put("jdbcUrl", hikariDataSource.getJdbcUrl());
//                 response.put("maximumPoolSize", hikariDataSource.getMaximumPoolSize());
//                 response.put("minimumIdle", hikariDataSource.getMinimumIdle());
//                 response.put("connectionTimeout", hikariDataSource.getConnectionTimeout() + "ms");
//                 response.put("idleTimeout", hikariDataSource.getIdleTimeout() + "ms");
//                 response.put("maxLifetime", hikariDataSource.getMaxLifetime() + "ms");
//                 response.put("leakDetectionThreshold", hikariDataSource.getLeakDetectionThreshold() + "ms");
//                 response.put("isRunning", !hikariDataSource.isClosed());
                
//                 return ResponseEntity.ok(response);
//             } else {
//                 response.put("status", "UNKNOWN");
//                 response.put("dataSourceType", dataSource.getClass().getSimpleName());
//                 response.put("message", "Not using HikariCP");
//                 return ResponseEntity.ok(response);
//             }
//         } catch (Exception e) {
//             response.put("status", "ERROR");
//             response.put("error", "Failed to get pool info: " + e.getMessage());
//             return ResponseEntity.status(500).body(response);
//         }
//     }
// } 