package com.smartcoach.spendwise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import com.smartcoach.spendwise.components.OAuth2SuccessHandler;
import com.smartcoach.spendwise.config.security.JwtUtil; // Import JwtUtil
import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity   
@RequiredArgsConstructor
public class SecurityBeansConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtUtil jwtUtil; // Inject JwtUtil

@Bean
public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint((exchange, e) -> {
                return Mono.fromRunnable(() -> 
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)
                );
            })
        )
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers(HttpMethod.OPTIONS).permitAll()
            .pathMatchers(
                "/api/auth/login", 
                "/api/auth/register", 
               
                "/api/webhook/**"
            ).permitAll()
            .pathMatchers("/api/auth/me").authenticated()
            .anyExchange().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
            .authenticationSuccessHandler(oAuth2SuccessHandler)
        )
        .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
            .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder()))
        )
        .build();
}

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        byte[] keyBytes = jwtUtil.getSecret().getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Location")); 
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}