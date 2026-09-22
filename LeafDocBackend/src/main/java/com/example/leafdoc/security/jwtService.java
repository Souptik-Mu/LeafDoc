package com.example.leafdoc.security;

import com.example.leafdoc.entity.User;
import com.example.leafdoc.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class jwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpiry;

    public jwtService(
            @Value("${app.jwt.secret}") String key,
            @Value("${app.jwt.expiration}") long expiration
    ) {
        secretKey = Keys.hmacShaKeyFor(
                //key.getBytes(StandardCharsets.UTF_8)
                Decoders.BASE64.decode(key)
        );

        accessTokenExpiry = expiration;
    }


    /// jwt contains : userId(as subject), role, iat(issued at), exp
    public String generateToken(
            User user
    ){
        Instant now = Instant.now();

                //.claim("userId", uId) //| to extract> return extractAllClaims(token).get("userId", Long.class);
        return Jwts.builder()
                .subject(user.getId().toString())  // | to extract> extractAllClaims(token).getSubject();
                .claim("role", user.getRole().name())
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
    

    public Long getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public Role getRoleFromToken(String token) {
        return extractAllClaims(token).get("role", Role.class);
    }
}
