package com.smartcoach.spendwise.config.security;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMillis;

    public String getSecret() {
        return secret;
    }

public String generateToken(String email, UUID userId, boolean onboarded) {

    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMillis);

    

    return Jwts.builder()
        .setSubject(email) // keep email as subject (matches your actual token)
        .claim("userId", userId.toString())
        .claim("onboarded", onboarded)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
        .compact();
}

}
