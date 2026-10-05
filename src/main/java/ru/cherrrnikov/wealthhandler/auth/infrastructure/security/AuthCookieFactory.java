package ru.cherrrnikov.wealthhandler.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AuthCookieFactory {
    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final JwtProperties jwtProperties;

    public ResponseCookie buildAccessTokenCookie(String token) {
        return buildCookie(ACCESS_TOKEN_COOKIE, token, Duration.ofMillis(jwtProperties.getAccessTokenExpiration()));

    }

    public ResponseCookie buildRefreshTokenCookie(String token) {
        return buildCookie(REFRESH_TOKEN_COOKIE, token, Duration.ofMillis(jwtProperties.getRefreshTokenExpiration()));

    }

    public ResponseCookie deleteAccessTokenCookie() {
        return buildCookie(ACCESS_TOKEN_COOKIE, "", Duration.ZERO);
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return buildCookie(REFRESH_TOKEN_COOKIE, "", Duration.ZERO);
    }

    private ResponseCookie buildCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
