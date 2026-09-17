package com.example.leafdoc.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || ! authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        /// here validate the token jwtService.isTokenValid(token) |create auth service


        try{
            if(jwtService.isTokenValid(token)){
                //String username = jwtService.getUsernameFromToken(token);
                /// like this take all user info from jwtService

            AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal();
                ///create principal with retreved info ,
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal,null);
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
            }

        }catch (JwtException | IllegalArgumentException e){
                SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

//    UsernamePasswordAuthenticationToken getAuthentication() {
//        return (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//
//        //UsernamePasswordAuthenticationToken authentication;
//        //authentication.setDetails(userId);
//        //return authentication;
//    }

}
