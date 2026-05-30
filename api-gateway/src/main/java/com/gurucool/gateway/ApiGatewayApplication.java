package com.gurucool.gateway;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@OpenAPIDefinition(
    info = @Info(
        title = "GuruCool API Gateway",
        version = "1.0.0",
        description = "Unified API Gateway for GuruCool — College Alumni Mentorship Platform",
        contact = @Contact(name = "GuruCool Team", email = "dev@gurucool.com")
    ),
    servers = @Server(url = "http://localhost:8080", description = "Local Gateway")
)
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
