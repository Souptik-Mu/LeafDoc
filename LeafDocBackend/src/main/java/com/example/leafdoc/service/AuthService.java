package com.example.leafdoc.service;

import com.example.leafdoc.DTO.LoginRequest;
import com.example.leafdoc.DTO.LoginResponse;
import com.example.leafdoc.DTO.RegisterRequest;
import com.example.leafdoc.entity.User;
import com.example.leafdoc.enums.Role;
import com.example.leafdoc.exceptions.InvalidCredentialsException;
import com.example.leafdoc.repository.UserRepository;
import com.example.leafdoc.security.jwtService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
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
    public String register(RegisterRequest request) {

        if (userRepo.existsByEmail(request.email()))
            throw new InvalidCredentialsException(); // todo: 409 - implement later

        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = new User();

        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setRole(Role.USER);

        User savedUser = userRepo.save(user);
        // need jwt here cause im doing auto log in.
        return jwtService.generateToken(savedUser);
    }
    
    //login
    public String login( @Valid LoginRequest request) {
        /// find user by email
        User user = userRepo
                .findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        /// check password
        if( !passwordEncoder.matches( request.password(), user.getPasswordHash() ) )
            throw new InvalidCredentialsException();

        /// genarate token
        /// send back the token
        return jwtService.generateToken(user);
    }
    //refresh
    //verifyOTP
    //forgotPass
    //resetPass
    //logout
}
