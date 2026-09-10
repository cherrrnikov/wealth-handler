package ru.cherrrnikov.wealthhandler.auth.application.port.in;

import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;

public interface GoogleAuthUseCase {
    AuthResult findOrCreateGoogleUser(String email, String username);
}
