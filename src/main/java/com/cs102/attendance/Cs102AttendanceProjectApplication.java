package com.cs102.attendance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Cs102AttendanceProjectApplication {

    private static final Logger logger = LoggerFactory.getLogger(Cs102AttendanceProjectApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Cs102AttendanceProjectApplication.class, args);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application is shutting down...");
            context.close();
            logger.info("Application shutdown complete.");
        }));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
