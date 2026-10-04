package ru.cherrrnikov.wealthhandler.auth.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.RoleRepository;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.UserRepository;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.domain.User;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.security.JwtService;
import ru.cherrrnikov.wealthhandler.common.exception.RoleNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleAuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private GoogleAuthService googleAuthService;

    @Test
    void findOrCreateGoogleUser_shouldFindGoogleUser_whenEmailAlreadyExists() {
        User user = User.builder()
                .email("igor@test.com")
                .username("igor")
                .build();

        when(userRepository.findByEmail("igor@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResult result = googleAuthService.findOrCreateGoogleUser(user.getEmail(), user.getUsername());

        assertNotNull(result);
        assertEquals("igor@test.com", result.user().getEmail());
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());

        verify(userRepository, never()).save(any());
    }

    @Test
    void findOrCreateGoogleUser_shouldCreateGoogleUser_whenEmailNotTaken() {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();

        when(userRepository.findByEmail("igor@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResult result = googleAuthService.findOrCreateGoogleUser("igor@test.com", "igor");

        assertNotNull(result);
        assertEquals("igor@test.com", result.user().getEmail());
        assertEquals("igor", result.user().getUsername());
        assertNull(result.user().getPasswordHash());
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void findOrCreateGoogleUser_shouldThrowException_whenRoleNotFound() {
        when(userRepository.findByEmail("igor@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> {
            googleAuthService.findOrCreateGoogleUser("igor@test.com", "igor");
        });
    }
}
