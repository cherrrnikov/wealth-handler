package ru.cherrrnikov.wealthhandler.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.GoogleAuthUseCase;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.RoleRepository;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.UserRepository;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.domain.User;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.security.JwtService;
import ru.cherrrnikov.wealthhandler.common.exception.RoleNotFoundException;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleAuthService implements GoogleAuthUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    @Override
    public AuthResult findOrCreateGoogleUser(String email, String username) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    Role role = roleRepository.findByName("ROLE_USER")
                            .orElseThrow(() -> new RoleNotFoundException("ROLE_USER"));

                    User newUser = User.builder()
                            .username(username)
                            .email(email)
                            .passwordHash(null)
                            .roles(Set.of(role))
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();

                    return userRepository.save(newUser);
                });

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthResult(user, newAccessToken, newRefreshToken);
    }
}