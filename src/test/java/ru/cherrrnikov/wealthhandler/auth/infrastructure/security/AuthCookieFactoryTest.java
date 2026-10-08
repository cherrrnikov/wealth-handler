package ru.cherrrnikov.wealthhandler.auth.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class AuthCookieFactoryTest {
    private AuthCookieFactory factory;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setAccessTokenExpiration(900000L);
        props.setRefreshTokenExpiration(604800000L);
        factory = new AuthCookieFactory(props);
    }

    @Test
    void buildAccessTokenCookie_shouldSetNameValueAndLifetime() {
        ResponseCookie cookie = factory.buildAccessTokenCookie("access-token");

        assertEquals("access_token", cookie.getName());
        assertEquals("access-token", cookie.getValue());
        assertEquals(Duration.ofMinutes(15), cookie.getMaxAge());
        assertSecurityAttributes(cookie);
    }

    @Test
    void buildRefreshTokenCookie_shouldSetNameValueAndLifetime() {
        ResponseCookie cookie = factory.buildRefreshTokenCookie("refresh-token");

        assertEquals("refresh_token", cookie.getName());
        assertEquals("refresh-token", cookie.getValue());
        assertEquals(Duration.ofDays(7), cookie.getMaxAge());
        assertSecurityAttributes(cookie);
    }

    @Test
    void deleteAccessTokenCookie_shouldExpireCookieAndKeepAttributes() {
        ResponseCookie cookie = factory.deleteAccessTokenCookie();

        assertEquals("access_token", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(Duration.ZERO, cookie.getMaxAge());
        assertSecurityAttributes(cookie);
    }

    @Test
    void deleteRefreshTokenCookie_shouldExpireCookieAndKeepAttributes() {
        ResponseCookie cookie = factory.deleteRefreshTokenCookie();

        assertEquals("refresh_token", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(Duration.ZERO, cookie.getMaxAge());
        assertSecurityAttributes(cookie);
    }

    private void assertSecurityAttributes(ResponseCookie cookie) {
        assertAll(
                () -> assertTrue(cookie.isHttpOnly(), "HttpOnly"),
                () -> assertTrue(cookie.isSecure(), "Secure"),
                () -> assertEquals("Strict", cookie.getSameSite(), "SameSite"),
                () -> assertEquals("/", cookie.getPath(), "Path")
        );
    }
}