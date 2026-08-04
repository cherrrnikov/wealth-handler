package ru.cherrrnikov.wealthhandler.auth.application.port.out;

import ru.cherrrnikov.wealthhandler.auth.domain.Role;

import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(String name);
}
