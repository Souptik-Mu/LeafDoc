package com.example.leafdoc.security;

import com.example.leafdoc.entity.User;
import com.example.leafdoc.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final Duration accessTokenExpiry;

    public JwtService(
            @Value("${app.jwt.secret}") String key,
            @Value("${app.jwt.expiration}") Duration expiration
    ) {
        secretKey = Keys.hmacShaKeyFor(
                //key.getBytes(StandardCharsets.UTF_8)
                Decoders.BASE64.decode(key)
        );

        accessTokenExpiry = expiration;
    }


    /// jwt contains : userId(as subject), sessionId(as id), role, iat(issued at), exp
     public record TokenData(
             Long userId,
             String sessionId,
             Role role
     ) {}

    public String generateToken(
            User user,
            String sessionId
    ){
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getId().toString())
                .id(sessionId) // jti (JWT ID)
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plus(accessTokenExpiry)
                        )
                )
                .signWith(secretKey)
                .compact();
    }

    public TokenData parseAndValidate(String token) throws IllegalArgumentException {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claims.getSubject();
        String sessionId = claims.getId();
        String roleValue = claims.get("role", String.class);
        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();

        if (
                     subject == null || subject.isBlank()
                || sessionId == null || sessionId.isBlank()
                || roleValue == null || roleValue.isBlank()
                || expiration == null
                || issuedAt == null
        ) {
            throw new IllegalArgumentException("Required JWT claims are missing");
        }

        Long userId = Long.parseLong(subject);
        Role role = Role.valueOf(roleValue);

        return new TokenData(
                userId,
                sessionId,
                role
        );
    }
}
