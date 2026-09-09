package ru.cherrrnikov.wealthhandler.auth.infrastructure.web.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.LoginUseCase;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.RefreshUseCase;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.RegisterUseCase;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.domain.User;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.security.JwtProperties;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.LoginRequest;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto.RegisterRequest;
import ru.cherrrnikov.wealthhandler.common.exception.EmailAlreadyExistsException;
import ru.cherrrnikov.wealthhandler.common.exception.InvalidCredentialsException;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RegisterUseCase registerUseCase;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private RefreshUseCase refreshUseCase;

    @Test
    void register_shouldReturn201_whenRegistrationSuccessful() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("igor@test.com")
                .username("igor")
                .password("rawPassword123")
                .build();

        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .email("igor@test.com")
                .username("igor")
                .roles(Set.of(role))
                .build();

        when(registerUseCase.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResult(user, "access-token", "refresh-token"));
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("igor@test.com"))
                .andExpect(jsonPath("$.username").value("igor"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("igor@test.com")
                .username("igor")
                .password("rawPassword123")
                .build();

        when(registerUseCase.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("igor@test.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email Already Exists"));
    }

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("not-an-email")
                .username("igor")
                .password("rawPassword123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn200_whenCredentialsValid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("igor@test.com")
                .password("rawPassword123")
                .build();

        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .email("igor@test.com")
                .username("igor")
                .roles(Set.of(role))
                .build();

        when(loginUseCase.login(any(LoginRequest.class)))
                .thenReturn(new AuthResult(user, "access-token", "refresh-token"));
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("igor@test.com"))
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().httpOnly("access_token", true));
    }

    @Test
    void login_shouldReturn401_whenCredentialsInvalid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("igor@test.com")
                .password("wrongPassword")
                .build();

        when(loginUseCase.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_shouldReturn200_whenRefreshTokenValid() throws Exception {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .email("igor@test.com")
                .username("igor")
                .roles(Set.of(role))
                .build();

        when(refreshUseCase.refresh(any(String.class)))
                .thenReturn(new AuthResult(user, "new-access-token", "new-refresh-token"));
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh_token", "some-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("igor@test.com"))
                .andExpect(cookie().exists("access_token"));
    }

    @Test
    void refresh_shouldReturn401_whenRefreshTokenInvalid() throws Exception {
        when(refreshUseCase.refresh(any(String.class)))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh_token", "bad-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturn204_andClearCookies() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().maxAge("refresh_token", 0));
    }
}
