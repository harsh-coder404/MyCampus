package com.example.minorapp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessExpiryMinutes;
    private final long refreshExpiryDays;

    public JwtUtil(
        @Value("${app.jwt.access-secret}") String accessSecret,
        @Value("${app.jwt.refresh-secret}") String refreshSecret,
        @Value("${app.jwt.access-expiry-minutes:120}") long accessExpiryMinutes,
        @Value("${app.jwt.refresh-expiry-days:30}") long refreshExpiryDays
    ) {
        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiryMinutes = accessExpiryMinutes;
        this.refreshExpiryDays = refreshExpiryDays;
    }

    public String generateAccessToken(String subject, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(subject)
            .claims(extraClaims)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessExpiryMinutes, ChronoUnit.MINUTES)))
            .signWith(accessKey)
            .compact();
    }

    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(refreshExpiryDays, ChronoUnit.DAYS)))
            .signWith(refreshKey)
            .compact();
    }

    public boolean isAccessTokenValid(String token) {
        try {
            parseAccessClaims(token);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public Claims parseAccessClaims(String token) {
        return Jwts.parser()
            .verifyWith(accessKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String extractEmail(String token) {
        return parseAccessClaims(token).getSubject();
    }
}


