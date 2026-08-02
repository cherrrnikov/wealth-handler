package ru.cherrrnikov.wealthhandler.auth.application.port.in;

import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.AuthResponse;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.RegisterRequest;

public interface RegisterUseCase {
    AuthResponse register(RegisterRequest registerRequest);
}
