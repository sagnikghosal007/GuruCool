package com.gurucool.mentorservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.gurucool.mentorservice", "com.gurucool.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableAsync
@EnableCaching
@OpenAPIDefinition(
    info = @Info(title = "GuruCool Mentor Service", version = "1.0.0",
        description = "Mentor profile management, availability, ratings, and verification"),
    servers = @Server(url = "http://localhost:8082")
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class MentorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MentorServiceApplication.class, args);
    }
}
