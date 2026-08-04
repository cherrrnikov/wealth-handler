package ru.cherrrnikov.wealthhandler.auth.infrastructure.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.LoginUseCase;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.RegisterUseCase;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.AuthResponse;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.LoginRequest;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.RegisterRequest;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = registerUseCase.register(registerRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = loginUseCase.login(loginRequest);

        return ResponseEntity.ok(response);
    }
}
