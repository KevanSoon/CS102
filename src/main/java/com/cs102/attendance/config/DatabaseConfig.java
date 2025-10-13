package com.cs102.attendance.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    private static final String TERMINATE_IDLE_CONNECTIONS = 
        "SELECT pg_terminate_backend(pid) " +
        "FROM pg_stat_activity " +
        "WHERE usename = current_user " +
        "AND state = 'idle' " +
        "AND pid <> pg_backend_pid()";
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Soft evicts all connections from the pool and forces their renewal
     */
    public void resetPool() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            try {
                logger.info("Initiating connection pool reset...");
                hikariDataSource.getHikariPoolMXBean().softEvictConnections();
                logger.info("HikariCP connections evicted and pool reset successfully");
            } catch (Exception e) {
                logger.error("Error resetting connection pool", e);
            }
        }
    }

    /**
     * Terminates idle database connections at the PostgreSQL level
     * @return The number of connections that were terminated
     */
    public int terminateIdleConnections() {
        try {
            int terminated = jdbcTemplate.update(TERMINATE_IDLE_CONNECTIONS);
            logger.info("Terminated {} idle database connections", terminated);
            return terminated;
        } catch (Exception e) {
            logger.error("Failed to terminate idle connections", e);
            return 0;
        }
    }

    @Scheduled(fixedRate = 900000) // Run every 15 minutes
    public void scheduleIdleConnectionCleanup() {
        terminateIdleConnections();
    }

    @EventListener(ContextClosedEvent.class)
    public void cleanupDataSource(ContextClosedEvent event) {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            logger.info("Closing HikariCP connection pool...");
            terminateIdleConnections(); // Terminate idle connections before closing
            hikariDataSource.close();
            logger.info("HikariCP connection pool has been closed");
        }
    }
}