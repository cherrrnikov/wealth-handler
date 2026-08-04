package ru.cherrrnikov.wealthhandler.auth.application.port.out;

import ru.cherrrnikov.wealthhandler.auth.domain.User;

import java.util.Optional;

public interface UserRepository{
    Optional<User> findByEmail(String email);

    User save(User user);

    boolean existsByEmail(String email);
}
