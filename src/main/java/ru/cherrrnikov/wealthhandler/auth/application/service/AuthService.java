package ru.cherrrnikov.wealthhandler.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.LoginUseCase;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.RegisterUseCase;
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

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService implements RegisterUseCase, LoginUseCase {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResult login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResult(user, jwtService.generateAccessToken(user), jwtService.generateRefreshToken(user));
    }

    @Override
    public AuthResult register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(registerRequest.getEmail());
        }

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("ROLE_USER"));

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(Set.of(role))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User savedUser = userRepository.save(user);

        return new AuthResult(savedUser, jwtService.generateAccessToken(savedUser), jwtService.generateRefreshToken(savedUser));
    }
}
