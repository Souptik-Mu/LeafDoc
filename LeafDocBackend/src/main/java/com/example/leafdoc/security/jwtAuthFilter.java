package com.example.leafdoc.security;

import com.example.leafdoc.enums.Role;
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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class jwtAuthFilter extends OncePerRequestFilter {

    private final jwtService jwtService;
    public jwtAuthFilter(jwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractTokenFromCookie(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try{
            if(jwtService.isTokenValid(token)){
                Long userId = jwtService.getUserIdFromToken(token);
                Role role = jwtService.getRoleFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        getUsernamePasswordAuthenticationToken(userId, role);

                SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
            }

        }catch (JwtException | IllegalArgumentException e){
            System.out.println("JWT Validation Failed: " + e.getMessage());
                SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private static @NonNull UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(Long userId, Role role) {
        AuthenticatedUserPrincipal principal =
                new AuthenticatedUserPrincipal(userId, role);

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" + role.name()
                );

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(authority)
        );
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
