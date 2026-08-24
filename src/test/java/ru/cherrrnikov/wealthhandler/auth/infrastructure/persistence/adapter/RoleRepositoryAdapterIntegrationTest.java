package ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.cherrrnikov.wealthhandler.AbstractIntegrationTest;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.RoleRepository;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoleRepositoryAdapterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByName_shouldReturnRole_whenRoleExists() {
        Optional<Role> result = roleRepository.findByName("ROLE_USER");

        assertTrue(result.isPresent());
        assertEquals("ROLE_USER", result.get().getName());
    }

    @Test
    void findByName_shouldReturnEmpty_whenRoleDoesNotExist() {
        Optional<Role> result = roleRepository.findByName("ROLE_NONEXISTENT");

        assertTrue(result.isEmpty());
    }
}