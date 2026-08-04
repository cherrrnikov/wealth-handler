package ru.cherrrnikov.wealthhandler.auth.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.PasswordEncoder;

@Component
public class BcryptPasswordEncoder implements PasswordEncoder {
    private final org.springframework.security.crypto.password.PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String encode(String password) {
        return encoder.encode(password);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
