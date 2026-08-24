package ru.cherrrnikov.wealthhandler.auth.infrastructure.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.*;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.domain.User;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private JwtService jwtService;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    public void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
        jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setAccessTokenExpiration(900000L);
        jwtProperties.setRefreshTokenExpiration(604800000L);

        jwtService = new JwtService(jwtEncoder, jwtDecoder, jwtProperties);
    }

    @Test
    void generateAccessToken_shouldContainCorrectSubjectAndRoles() {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .email("igor@test.com")
                .roles(Set.of(role))
                .build();

        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertEquals("igor@test.com", jwtService.extractEmail(token));
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsInvalid() {
        String invalidToken = "invalid";

        boolean isValid = jwtService.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void generateRefreshToken_shouldNotContainRolesClaim() {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .email("igor@test.com")
                .roles(Set.of(role))
                .build();

        String refreshToken = jwtService.generateRefreshToken(user);
        Jwt decodedToken = jwtDecoder.decode(refreshToken);

        assertEquals("igor@test.com", decodedToken.getSubject());
        assertNull(decodedToken.getClaim("roles"));
    }

    @Test
    void validateToken_shouldReturnFalse_whenSignedWithDifferentKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair anotherKeyPair = generator.generateKeyPair();

        RSAPublicKey anotherPublicKey = (RSAPublicKey) anotherKeyPair.getPublic();
        RSAPrivateKey anotherPrivateKey = (RSAPrivateKey) anotherKeyPair.getPrivate();

        JWK anotherJwk = new RSAKey.Builder(anotherPublicKey).privateKey(anotherPrivateKey).build();
        JWKSource<SecurityContext> anotherJwkSource = new ImmutableJWKSet<>(new JWKSet(anotherJwk));
        JwtEncoder anotherEncoder = new NimbusJwtEncoder(anotherJwkSource);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("igor@test.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        String tokenSignedByAnotherKey = anotherEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        boolean isValid = jwtService.validateToken(tokenSignedByAnotherKey);

        assertFalse(isValid);
    }
}
