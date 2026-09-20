package com.example.leafdoc.service;

import com.example.leafdoc.DTO.LoginRequest;
import com.example.leafdoc.DTO.LoginResponse;
import com.example.leafdoc.entity.User;
import com.example.leafdoc.repository.UserRepository;
import com.example.leafdoc.security.jwtService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final jwtService jwtService;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, jwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    //register
    
    //login
    public String login( @Valid LoginRequest request) {
        /// find user by email
        User user = userRepo
                .findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new); // custom exception

        /// check password
        if( !passwordEncoder.matches( request.password(), user.getPassword() ))
            throw new InvalidCredentialsException();

        /// genarate token

        String accessToken = jwtService.generateToken(user); // genarate token
        /// send back the token
        return null;
    }
    //refresh
    //verifyOTP
    //forgotPass
    //resetPass
    //logout
}
