package ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
