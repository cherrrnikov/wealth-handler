package ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.cherrrnikov.wealthhandler.AbstractIntegrationTest;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.RoleRepository;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.UserRepository;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.domain.User;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryAdapterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void saveAndFindByEmail_shouldPersistAndRetrieveUser() {
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow();

        User user = User.builder()
                .email("igor@test.com")
                .username("igor")
                .passwordHash("hashedPassword")
                .roles(Set.of(role))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findByEmail("igor@test.com");

        assertNotNull(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("igor@test.com", foundUser.get().getEmail());
        assertEquals("igor", foundUser.get().getUsername());
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenUserDoesNotExist() {
        Optional<User> result = userRepository.findByEmail("nonexistent@test.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenUserExists() {
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow();

        User user = User.builder()
                .email("existing@test.com")
                .username("existinguser")
                .passwordHash("hashedPassword")
                .roles(Set.of(role))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("existing@test.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenUserDoesNotExist() {
        boolean exists = userRepository.existsByEmail("nobody@test.com");

        assertFalse(exists);
    }

    @Test
    @Transactional
    void save_shouldPersistUserRolesAssociation() {
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow();

        User user = User.builder()
                .email("withroles@test.com")
                .username("withroles")
                .passwordHash("hashedPassword")
                .roles(Set.of(role))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        userRepository.save(user);
        User foundUser = userRepository.findByEmail("withroles@test.com").orElseThrow();

        assertTrue(foundUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_USER")));
    }
}