package com.example.leafdoc.security;

import java.util.UUID;

public record AuthenticatedUserPrincipal(
        //user id, email, role?
        UUID userId,
        String role
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