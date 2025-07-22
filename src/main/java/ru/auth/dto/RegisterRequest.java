package ru.auth.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50)
        String username,

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 6, max = 100)
        String password,

        Set<String> roles
) {}