package ru.cherrrnikov.wealthhandler.auth.application.port.in;

import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.RegisterRequest;

public interface RegisterUseCase {
    AuthResult register(RegisterRequest registerRequest);
}
