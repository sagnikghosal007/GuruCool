package com.gurucool.aiservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.gurucool.aiservice", "com.gurucool.common"})
@EnableDiscoveryClient
@EnableAsync
@EnableCaching
@OpenAPIDefinition(
    info = @Info(title = "GuruCool AI Service", version = "1.0.0",
        description = "AI mentor matching, career paths, skill gap analysis. Uses OpenAI if key present, falls back to mock responses."),
    servers = @Server(url = "http://localhost:8086")
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class AiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
