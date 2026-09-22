package com.example.leafdoc.security;

import com.example.leafdoc.enums.Role;

public record AuthenticatedUserPrincipal(
        //user id, email, role?
        Long userId,
        Role role
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