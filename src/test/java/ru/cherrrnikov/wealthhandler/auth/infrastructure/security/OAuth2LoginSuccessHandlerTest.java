package ru.cherrrnikov.wealthhandler.auth.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.GoogleAuthUseCase;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.domain.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private GoogleAuthUseCase googleAuthUseCase;

    @Mock
    private JwtProperties jwtProperties;

    @Test
    void onAuthenticationSuccess_shouldSetTokenCookies_redirect_andInvalidateSession() throws Exception {
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L);

        User user = User.builder()
                .email("igor@test.com")
                .username("Igor")
                .roles(Set.of(Role.builder().id(1L).name("ROLE_USER").build()))
                .build();
        when(googleAuthUseCase.findOrCreateGoogleUser("igor@test.com", "Igor"))
                .thenReturn(new AuthResult(user, "access-token", "refresh-token"));

        OAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", "igor@test.com", "name", "Igor"),
                "email");
        Authentication authentication =
                new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2LoginSuccessHandler handler =
                new OAuth2LoginSuccessHandler(googleAuthUseCase, new AuthCookieFactory(jwtProperties));

        handler.onAuthenticationSuccess(request, response, authentication);

        assertEquals("/", response.getRedirectedUrl());
        assertEquals(2, response.getHeaders(HttpHeaders.SET_COOKIE).size());
        assertTrue(session.isInvalid());
    }
}