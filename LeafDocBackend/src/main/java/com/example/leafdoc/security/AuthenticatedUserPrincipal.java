package com.example.leafdoc.security;

import com.example.leafdoc.enums.Role;

public record AuthenticatedUserPrincipal(
        Long userId,
        Role role,
        String sessionId
) {
}

/*
*
* later can be done like this:
*  @AuthenticatedUserPrincipal
* AuthenticatedUserPrincipal userPrincipal
*
*      now this userPrincipal provides current authenticated user data.
*
* */