package com.example.leafdoc.confiurations;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class jwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || ! authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        // here validate the token jwtService.isTokenValid(token) |create auth service

        UsernamePasswordAuthenticationToken authentication = null
                //getUserAuthenticationToken(role, email, userId);

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

    }

    UsernamePasswordAuthenticationToken getAuthentication() {
        return (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UsernamePasswordAuthenticationToken authentication;
        //authentication.setDetails(userId);
        return authentication;
    }
}
