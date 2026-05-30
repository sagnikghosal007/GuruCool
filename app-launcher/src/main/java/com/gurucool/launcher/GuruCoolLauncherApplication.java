package com.gurucool.launcher;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * GuruCool All-in-One Launcher
 *
 * Runs ALL microservices in a single JVM for local development.
 * Uses:
 *   - H2 in-memory database (no PostgreSQL needed)
 *   - Embedded Apache Kafka (no Kafka server needed)
 *   - In-memory cache (no Redis needed)
 *   - MockPaymentEngine (no payment gateway needed)
 *   - MockAiEngine (no OpenAI key needed)
 *
 * Access: http://localhost:8080/swagger-ui.html
 */
@SpringBootApplication(scanBasePackages = {
    "com.gurucool.launcher",
    "com.gurucool.common",
    "com.gurucool.userservice",
    "com.gurucool.mentorservice",
    "com.gurucool.sessionservice",
    "com.gurucool.paymentservice",
    "com.gurucool.notificationservice",
    "com.gurucool.aiservice"
})
@EnableJpaAuditing
@EnableAsync
@EnableCaching
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "GuruCool API — All-in-One",
        version = "1.0.0",
        description = "Complete GuruCool backend running in a single JVM (local dev mode). All APIs available here.",
        contact = @Contact(name = "GuruCool Team", email = "dev@gurucool.com")
    ),
    servers = @Server(url = "http://localhost:8080", description = "Local All-in-One Server")
)
public class GuruCoolLauncherApplication {

    public static void main(String[] args) {
        // Set local profile so embedded infra is used
        System.setProperty("spring.profiles.active", "local");
        SpringApplication.run(GuruCoolLauncherApplication.class, args);
    }
}
