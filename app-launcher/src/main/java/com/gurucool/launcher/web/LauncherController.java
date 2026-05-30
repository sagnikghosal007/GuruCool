package com.gurucool.launcher.web;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Hidden
@RestController
public class LauncherController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("app", "GuruCool Backend");
        info.put("mode", "All-in-One Local Dev");
        info.put("swagger", "http://localhost:8080/swagger-ui.html");
        info.put("health", "http://localhost:8080/actuator/health");
        info.put("services", Map.of(
            "auth",     "POST /api/users/auth/register  |  POST /api/users/auth/login",
            "mentors",  "GET  /api/mentors              |  POST /api/mentors/profile",
            "sessions", "GET  /api/sessions             |  POST /api/sessions",
            "payments", "POST /api/payments/test/simulate (no real money)",
            "ai",       "POST /api/ai/match             |  POST /api/ai/career-path"
        ));
        return info;
    }
}
