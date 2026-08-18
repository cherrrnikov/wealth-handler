package ru.cherrrnikov.wealthhandler.auth.infrastructure.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.LoginUseCase;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.RegisterUseCase;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.security.JwtProperties;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.LoginRequest;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.RegisterRequest;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.UserResponse;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResult result = registerUseCase.register(registerRequest);

        return buildResponse(result, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResult result = loginUseCase.login(loginRequest);

        return buildResponse(result, HttpStatus.OK);
    }

    private ResponseEntity<UserResponse> buildResponse(AuthResult result, HttpStatus status) {
        ResponseCookie accessCookie = buildCookie("access_token", result.accessToken(),
                Duration.ofMillis(jwtProperties.getAccessTokenExpiration())
                );
        ResponseCookie refreshCookie = buildCookie("refresh_token", result.refreshToken(),
                Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())
                );

        UserResponse body = new UserResponse(
                result.user().getEmail(),
                result.user().getUsername(),
                result.user().getRoles().stream().map(Role::getName).toList()
        );

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(body);
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
