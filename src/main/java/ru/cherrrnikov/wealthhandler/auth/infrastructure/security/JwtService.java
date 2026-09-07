package ru.cherrrnikov.wealthhandler.auth.infrastructure.security;

import org.springframework.security.oauth2.jwt.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.domain.User;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getEmail())
                .claim("roles", user.getRoles().stream()
                        .map(Role::getName)
                        .toList()
                )
                .claim("type", "access")
                .issuedAt(now)
                .expiresAt(now.plusMillis(jwtProperties.getAccessTokenExpiration()))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getEmail())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiresAt(now.plusMillis(jwtProperties.getRefreshTokenExpiration()))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);

            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return jwtDecoder.decode(token).getSubject();
    }

    public String extractType(String token) {
        return jwtDecoder.decode(token).getClaim("type");
    }
}
