package com.cs102.attendance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // First, terminate all idle connections
            String terminateIdleConnections = 
                "SELECT pg_terminate_backend(pid) " +
                "FROM pg_stat_activity " +
                "WHERE usename = current_user " +
                "AND state = 'idle' " +
                "AND pid <> pg_backend_pid()";
            
            int terminated = jdbcTemplate.update(terminateIdleConnections);
            logger.info("Terminated {} idle database connections", terminated);

            // Then check remaining connections
            String checkConnections = 
                "SELECT count(*) FROM pg_stat_activity " +
                "WHERE usename = current_user";
            
            int activeConnections = jdbcTemplate.queryForObject(checkConnections, Integer.class);
            logger.info("Current active connections: {}", activeConnections);

        } catch (Exception e) {
            logger.error("Error initializing database connections", e);
        }
    }
}