package ru.cherrrnikov.wealthhandler.auth.application.dto;

import ru.cherrrnikov.wealthhandler.auth.domain.User;

public record AuthResult(User user, String accessToken, String refreshToken) {
}
