package ru.cherrrnikov.wealthhandler.auth.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ru.cherrrnikov.wealthhandler.auth.application.dto.AuthResult;
import ru.cherrrnikov.wealthhandler.auth.application.port.in.GoogleAuthUseCase;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final GoogleAuthUseCase googleAuthUseCase;
    private final AuthCookieFactory authCookieFactory;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        AuthResult result = googleAuthUseCase.findOrCreateGoogleUser(email, name);

        ResponseCookie accessCookie = authCookieFactory.buildAccessTokenCookie(result.accessToken());
        ResponseCookie refreshCookie = authCookieFactory.buildRefreshTokenCookie(result.refreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        response.sendRedirect("/");
    }
}
