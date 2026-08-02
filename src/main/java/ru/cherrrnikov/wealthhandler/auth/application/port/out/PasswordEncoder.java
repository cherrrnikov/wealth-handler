package ru.cherrrnikov.wealthhandler.auth.application.port.out;

public interface PasswordEncoder {
    String encode(String password);

    boolean matches(String rawPassword, String encodedPassword);
}
