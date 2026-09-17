package com.example.leafdoc.security;

public record AuthenticatedUserPrincipal(
        //user id, email, role?
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