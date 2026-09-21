package com.example.leafdoc.security;

import com.example.leafdoc.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class jwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpiry;

    public jwtService(
            @Value("${app.jwt.secret}") String key,
            @Value("${app.jwt.expiration}") long expiration
    ) {
        secretKey = Keys.hmacShaKeyFor(
                key.getBytes(StandardCharsets.UTF_8)
        );

        accessTokenExpiry = expiration;
    }


    /// jwt contains : userId(as subject), role, iat(issued at), exp
    public String generateToken(
            User user
    ){
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getId())  // | to extract> extractAllClaims(token).getSubject();
                //.claim("userId", uId) //| to extract> return extractAllClaims(token).get("userId", Long.class);
                .claim("role", user.getRole())
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plusMillis(accessTokenExpiry)
                        )
                )
                .signWith(secretKey)
                .compact();
    }
    public boolean isTokenValid(String token){
        try{
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        }catch(JwtException | IllegalArgumentException e ){
            return false;
        }
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    ///  take user info from db, return the principal.
    

    public UUID getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return (UUID) claims.get("userId");
    }

    public String getEmailFromToken(String token) {
    }

    public String getRoleFromToken(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
}
