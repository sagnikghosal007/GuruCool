package com.gurucool.userservice.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record RegisterRequest(
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    String fullName,

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    String email,

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$",
        message = "Password must be at least 8 characters with uppercase, number, and special character (@#$%^&+=!)"
    )
    String password,

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "STUDENT|MENTOR", message = "Role must be STUDENT or MENTOR")
    String role,

    UUID collegeId
) {}
