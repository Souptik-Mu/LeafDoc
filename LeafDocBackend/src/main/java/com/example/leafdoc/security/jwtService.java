package com.example.leafdoc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

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


    /// jwt contains : userId(as subject),
    public String generateToken(
            Long uId, String email
            //todo: later take a login object or user
    ){
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(email)  // user.getemail | to extract> extractAllClaims(token).getSubject();
                .claim("userId", uId) //| to extract> return extractAllClaims(token).get("userId", Long.class);
                //.claim("role", user.getRole())
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
    public void extractUserName(String token){}
    public void extractPassword(String token){}

}
