package com.example.leafdoc.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class jwtAuthFilter extends OncePerRequestFilter {

    private final jwtService jwtService;
    public jwtAuthFilter(jwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractTokenFromCookie(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try{
            if(jwtService.isTokenValid(token)){
                UUID userId = jwtService.getUserIdFromToken(token);
                String role = jwtService.getRoleFromToken(token);
                //String email = jwtService.getEmailFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        getUsernamePasswordAuthenticationToken(userId, role);

                SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
            }

        }catch (JwtException | IllegalArgumentException e){
                SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private static @NonNull UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(UUID userId, String role) {
        AuthenticatedUserPrincipal principal =
                new AuthenticatedUserPrincipal(userId, role);

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" + role
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(authority)
                );
        return authentication;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {

        if(request.getCookies() == null)
            return null;

        for (Cookie cookie : request.getCookies())
            if (cookie.getName().equals("accessToken"))
                return cookie.getValue();

        return null;
    }
}
