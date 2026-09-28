package com.example.leafdoc.service;

import com.example.leafdoc.DTO.auth.LoginRequest;
import com.example.leafdoc.DTO.auth.RegisterRequest;
import com.example.leafdoc.entity.PendingRegistration;
import com.example.leafdoc.entity.User;
import com.example.leafdoc.enums.Role;
import com.example.leafdoc.exceptions.InvalidCredentialsException;
import com.example.leafdoc.repository.PendingRegistrationRepo;
import com.example.leafdoc.repository.UserRepository;
import com.example.leafdoc.security.jwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PendingRegistrationRepo pendingRepo;
    private final PasswordEncoder passwordEncoder;
    private final jwtService jwtService;
    private final EmailService emailService;


    //register
    public void register(RegisterRequest request) {

        if (userRepo.existsByEmail(request.email()))
            throw new InvalidCredentialsException(); // todo: 409 - implement later

        if (pendingRepo.existsByEmail(request.email()))
            throw new InvalidCredentialsException();

        //* generate random token
        //  hash token
        //  create and save a pending Registration
        // send email (mail service) with the link containing token
        //!-----------------------------------------------------------
        // ok clicking that link another endpoint triggers(somewhere that's not here) [verify-user]

    }
    //verify
    public String verifyToken(String token) {
        //*     here token hashed, and corosponding pendin registration found.
        //      after checking expiry, and other things.
        //      create user aggainst that, and consume the pending registration
        //      genarate jwt now and return from there (trigger a redirect to main page)




        {//Creation of user
            String passwordHash =
                    passwordEncoder.encode(request.password());

            User user = new User();

            user.setName(request.name());
            user.setEmail(request.email());
            user.setPasswordHash(
                    passwordEncoder.encode(request.password())
            );
            user.setRole(Role.USER);
        }

        User savedUser = userRepo.save(user);
        /// need jwt here cause I'm doing auto log in. (return jwt for auto login)
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

    //verifyOTP
    //forgotPass
    //resetPass
    //logout
}
