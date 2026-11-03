package com.example.leafdoc.security;

import com.example.leafdoc.entity.UserSession;
import com.example.leafdoc.enums.Role;
import com.example.leafdoc.repository.UserSessionRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class jwtAuthFilter extends OncePerRequestFilter {

    private final jwtService jwtService;
    private final UserSessionRepository sessionRepo;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String headerToken = extractTokenFromHeader(request);
        String cookieToken = extractTokenFromCookie(request);

        if (headerToken != null && cookieToken != null && !headerToken.equals(cookieToken)) {
                rejectRequest(response,"Conflicting authentication credentials");
            return;
        }

        String token = headerToken != null ? headerToken : cookieToken;

        /// No credentials: allow anonymous access
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try{
            jwtService.TokenData tokenData = jwtService.parseAndValidate(token);

            Long userId = tokenData.userId();
            String sessionId = tokenData.sessionId();
            Role role = tokenData.role();

            Optional<UserSession> session = sessionRepo.findActiveSession(sessionId,userId, Instant.now());

            if( session.isEmpty() ){
                    rejectRequest(response,"Session expired or revoked");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    getUsernamePasswordAuthenticationToken(userId, role);

            SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        }catch (JwtException | IllegalArgumentException e){
            System.out.println("JWT Validation Failed: " + e.getMessage());
                rejectRequest(response,"Invalid or expired token");
            return;
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

        Cookie[] cookies = request.getCookies();

        if(cookies == null)
            return null;

        for (Cookie cookie : cookies)
            if ("accessToken".equals(cookie.getName())){
                String token = cookie.getValue();
                return (token == null || token.isBlank()) ? null : token;
            }

        return null;
    }

    private String extractTokenFromHeader(HttpServletRequest request) {

        String header = request.getHeader("Authorization");
        if (
                header != null &&
                header.regionMatches(true, 0,"Bearer ", 0, 7)
        ) {
            return header.substring(7);
        }
        return null;
    }

    private void rejectRequest(
            HttpServletResponse response,
            String message
    ) throws IOException {

        SecurityContextHolder.clearContext();

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                message
        );
    }
}
