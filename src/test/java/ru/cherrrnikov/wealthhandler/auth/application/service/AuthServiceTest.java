package ru.cherrrnikov.wealthhandler.auth.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.PasswordEncoder;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.RoleRepository;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.UserRepository;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.domain.User;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.security.JwtService;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.LoginRequest;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.RegisterRequest;
import ru.cherrrnikov.wealthhandler.common.exception.EmailAlreadyExistsException;
import ru.cherrrnikov.wealthhandler.common.exception.InvalidCredentialsException;
import ru.cherrrnikov.wealthhandler.common.exception.RoleNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUser_whenEmailNotTaken() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("igor@test.com")
                .username("igor")
                .password("rawPassword123")
                .build();

        Role role = Role.builder().id(1L).name("ROLE_USER").build();

        when(userRepository.findByEmail("igor@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("rawPassword123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResult result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("igor@test.com", result.user().getEmail());
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("igor@test.com")
                .username("igor")
                .password("rawPassword123")
                .build();

        User existingUser = User.builder()
                .email("igor@test.com")
                .build();

        when(userRepository.findByEmail("igor@test.com")).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });
    }

    @Test
    void register_shouldThrowException_whenRoleNotFound() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("igor@test.com")
                .username("igor")
                .password("rawPassword123")
                .build();

        when(userRepository.findByEmail("igor@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> {
            authService.register(registerRequest);
        });
    }

    @Test
    void login_shouldLoginUser_whenCredentialsAreValid() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("igor@test.com")
                .password("rawPassword123")
                .build();

        User existingUser = User.builder()
                .email("igor@test.com")
                .passwordHash("hashedPassword")
                .build();

        when(userRepository.findByEmail("igor@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResult result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("igor@test.com", result.user().getEmail());
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("no@test.com")
                .build();

        when(userRepository.findByEmail("no@test.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void login_shouldThrowException_whenPasswordInvalid() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("igor@test.com")
                .password("rawPassword123")
                .build();

        User existingUser = User.builder()
                .email("igor@test.com")
                .passwordHash("hashedPassword")
                .build();

        when(userRepository.findByEmail("igor@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void refresh_shouldReturnNewTokens_whenRefreshTokenValid() {
        String refreshToken = "valid-refresh-token";
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .email("igor@test.com")
                .username("igor")
                .roles(Set.of(role))
                .build();

        when(jwtService.validateToken(refreshToken)).thenReturn(true);
        when(jwtService.extractType(refreshToken)).thenReturn("refresh");
        when(jwtService.extractEmail(refreshToken)).thenReturn("igor@test.com");
        when(userRepository.findByEmail("igor@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");

        AuthResult result = authService.refresh(refreshToken);

        assertNotNull(result);
        assertEquals("igor@test.com", result.user().getEmail());
        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-refresh-token", result.refreshToken());
    }

    @Test
    void refresh_shouldThrowException_whenTokenInvalid() {
        String refreshToken = "invalid-token";

        when(jwtService.validateToken(refreshToken)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.refresh(refreshToken);
        });
    }

    @Test
    void refresh_shouldThrowException_whenTokenTypeIsNotRefresh() {
        String accessToken = "access-token-used-as-refresh";

        when(jwtService.validateToken(accessToken)).thenReturn(true);
        when(jwtService.extractType(accessToken)).thenReturn("access");

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.refresh(accessToken);
        });
    }
}
