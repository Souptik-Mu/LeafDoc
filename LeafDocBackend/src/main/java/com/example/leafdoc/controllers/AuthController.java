package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.request.LoginRequest;
import com.example.leafdoc.security.AuthenticatedUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public void login(@RequestBody LoginRequest req) {
        // get user from req | dependency : userRepo
        // check password by passEncoder.matches()
    }
    @PostMapping("/register")
    public void register(@RequestBody LoginRequest temp) {

    }
//    @GetMapping("/me")
//    public UserResponse me(
//            @AuthenticationPrincipal
//            AuthenticatedUserPrincipal user
//    ) {
//        return userService.getUser(user.userId());
//    }

}
