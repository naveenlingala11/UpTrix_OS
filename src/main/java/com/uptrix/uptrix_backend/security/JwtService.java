package com.uptrix.uptrix_backend.security;

import com.uptrix.uptrix_backend.entity.Role;
import com.uptrix.uptrix_backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${uptrix.jwt.secret}")
    private String secret;

    @Value("${uptrix.jwt.expiration-ms}")
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("EMPLOYEE");

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("cid", user.getCompany().getId())
                .claim("role", primaryRole)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        String subject = claims.getSubject();
        return Long.valueOf(subject);
    }

    public Long extractCompanyId(String token) {
        Claims claims = parseClaims(token);
        Object cid = claims.get("cid");
        return cid != null ? Long.valueOf(cid.toString()) : null;
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
