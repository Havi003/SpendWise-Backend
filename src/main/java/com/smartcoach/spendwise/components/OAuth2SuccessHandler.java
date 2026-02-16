package com.smartcoach.spendwise.components;

import java.net.URI;
import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.smartcoach.spendwise.config.security.JwtUtil;
import com.smartcoach.spendwise.domain.entity.User;
import com.smartcoach.spendwise.service.AuthService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements ServerAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> onAuthenticationSuccess(
            WebFilterExchange exchange,
            Authentication authentication) {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String fullName = oAuth2User.getAttribute("name");

        return authService.findOrCreateGoogleUser(email, fullName)
            .flatMap(result -> {

                User user = result.user();

                boolean onboarded = Boolean.TRUE.equals(user.isOnboarded());

                log.info("OAuth2 login success → email={}, onboarded={}", user.getEmail(), onboarded);

                String jwt = jwtUtil.generateToken(
                        user.getEmail(),
                        user.getId(),
                        onboarded
                );

                ServerHttpResponse response = exchange.getExchange().getResponse();

                ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", jwt)
                        .httpOnly(true)
                        .secure(false) // true in production with HTTPS
                        .path("/")
                        .maxAge(Duration.ofDays(1))
                        .sameSite("Lax")
                        .build();

                response.addCookie(cookie);

                String redirectUrl = onboarded
                        ? "http://localhost:3000/dashboard"
                        : "http://localhost:3000/onboarding";

                response.setStatusCode(HttpStatus.FOUND);
                response.getHeaders().setLocation(URI.create(redirectUrl));

                return response.setComplete();
            });
    }
}
