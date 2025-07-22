package ru.auth.dto;

public record ErrorResponse(
        String field,
        String message
) {}
