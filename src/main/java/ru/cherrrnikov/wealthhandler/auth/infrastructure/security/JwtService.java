package ru.cherrrnikov.wealthhandler.auth.infrastructure.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.domain.User;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("roles", user.getRoles().stream()
                        .map(Role::getName)
                        .toList()
                )
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() +
                        jwtProperties.getAccessTokenExpiration()
                        ))
                .signWith(jwtProperties.getPrivateKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() +
                        jwtProperties.getRefreshTokenExpiration()
                ))
                .signWith(jwtProperties.getPrivateKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            jwtParser().parseSignedClaims(token);

            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return jwtParser()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private JwtParser jwtParser() {
        return Jwts.parser()
                .verifyWith(jwtProperties.getPublicKey())
                .build();
    }
}
