package com.cs102.attendance.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;

@Configuration
public class DatabaseConfig {

    @Autowired
    private DataSource dataSource;

    @PreDestroy
    public void cleanup() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            System.out.println("HikariCP connection pool has been closed");
        }
    }

    @Bean
    public javax.sql.DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        // Configuration will be picked up from application.yml
        ds.setRegisterMbeans(true); // Enable JMX monitoring
        return ds;
    }
}