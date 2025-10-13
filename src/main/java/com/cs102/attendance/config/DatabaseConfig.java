package com.cs102.attendance.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {
    @Autowired
    private DataSource dataSource;

    @EventListener(ContextClosedEvent.class)
    public void cleanupDataSource(ContextClosedEvent event) {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            System.out.println("Closing HikariCP connection pool...");
            hikariDataSource.close();
            System.out.println("HikariCP connection pool has been closed");
        }
    }
}