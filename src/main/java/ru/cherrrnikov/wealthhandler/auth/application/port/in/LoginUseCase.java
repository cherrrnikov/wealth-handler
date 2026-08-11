package ru.cherrrnikov.wealthhandler.auth.application.port.in;

import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.LoginRequest;

public interface LoginUseCase {
    AuthResult login(LoginRequest loginRequest);
}
