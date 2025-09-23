package com.cs102.attendance.controller;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> checkDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Test the connection
            boolean isValid = connection.isValid(5); // 5 second timeout
            
            if (isValid) {
                response.put("status", "UP");
                response.put("database", "Connected");
                response.put("url", connection.getMetaData().getURL());
                response.put("driver", connection.getMetaData().getDriverName());
                response.put("version", connection.getMetaData().getDatabaseProductVersion());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "DOWN");
                response.put("database", "Connection invalid");
                return ResponseEntity.status(503).body(response);
            }
        } catch (SQLException e) {
            response.put("status", "DOWN");
            response.put("database", "Connection failed");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/connection-pool")
    public ResponseEntity<Map<String, Object>> checkConnectionPool() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                
                // Get HikariCP MBean for detailed metrics
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (AttendanceHikariCP)");
                HikariPoolMXBean poolProxy = JMX.newMXBeanProxy(server, poolName, HikariPoolMXBean.class);
                
                response.put("status", "UP");
                response.put("poolName", "AttendanceHikariCP");
                response.put("activeConnections", poolProxy.getActiveConnections());
                response.put("idleConnections", poolProxy.getIdleConnections());
                response.put("totalConnections", poolProxy.getTotalConnections());
                response.put("threadsAwaitingConnection", poolProxy.getThreadsAwaitingConnection());
                
                // Configuration details
                response.put("maximumPoolSize", hikariDataSource.getMaximumPoolSize());
                response.put("minimumIdle", hikariDataSource.getMinimumIdle());
                response.put("connectionTimeout", hikariDataSource.getConnectionTimeout());
                response.put("idleTimeout", hikariDataSource.getIdleTimeout());
                response.put("maxLifetime", hikariDataSource.getMaxLifetime());
                response.put("leakDetectionThreshold", hikariDataSource.getLeakDetectionThreshold());
                
                // Health indicators
                int activeConnections = poolProxy.getActiveConnections();
                int totalConnections = poolProxy.getTotalConnections();
                int maxPoolSize = hikariDataSource.getMaximumPoolSize();
                
                response.put("poolUtilization", String.format("%.2f%%", 
                    (double) totalConnections / maxPoolSize * 100));
                response.put("activeUtilization", String.format("%.2f%%", 
                    (double) activeConnections / maxPoolSize * 100));
                
                // Warning thresholds
                if (totalConnections >= maxPoolSize * 0.9) {
                    response.put("warning", "Connection pool is near maximum capacity");
                }
                if (poolProxy.getThreadsAwaitingConnection() > 0) {
                    response.put("warning", "Threads are waiting for connections - possible bottleneck");
                }
                
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "UNKNOWN");
                response.put("message", "DataSource is not HikariCP");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", "Failed to get connection pool metrics: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/connection-test")
    public ResponseEntity<Map<String, Object>> testConnectionLeak() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test multiple rapid connections to check for leaks
            for (int i = 0; i < 3; i++) {
                try (Connection connection = dataSource.getConnection()) {
                    connection.isValid(1);
                }
            }
            
            response.put("status", "UP");
            response.put("message", "Connection leak test passed");
            response.put("testConnections", 3);
            return ResponseEntity.ok(response);
            
        } catch (SQLException e) {
            response.put("status", "DOWN");
            response.put("error", "Connection leak test failed: " + e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }
} 