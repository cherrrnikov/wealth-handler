package ru.cherrrnikov.wealthhandler.auth.application.port.in;

import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;

public interface RefreshUseCase {
    AuthResult refresh(String refreshToken);
}
