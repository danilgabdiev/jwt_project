package ru.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}