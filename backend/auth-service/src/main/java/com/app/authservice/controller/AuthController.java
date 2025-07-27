package com.app.authservice.controller;

import com.app.authservice.dto.*;
import com.app.authservice.model.User;
import com.app.authservice.model.VerificationToken;
import com.app.authservice.service.*;
import com.app.authservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private VerificationTokenService tokenService;
    @Autowired private EmailService emailService;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Password and Confirm Password do not match");
        }

        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        try {
            User user = userService.registerUser(request.getEmail(), request.getPassword());
            VerificationToken token = tokenService.createVerificationToken(user);
            String verificationUrl = "http://localhost:8081/api/auth/verify?token=" + token.getToken();

            log.info("Sending verification email to {}", user.getEmail());
            emailService.sendVerificationEmail(user, token.getToken(), verificationUrl);

            return ResponseEntity.ok("Registration successful! Check your email for verification link.");
        } catch (Exception e) {
            log.error("Error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
        }
    }

    @GetMapping("/verify")
    public void verifyUser(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        log.info("Verifying token: {}", token);

        VerificationToken verificationToken = tokenService.findByToken(token).orElse(null);
        if (verificationToken == null || tokenService.isTokenExpired(verificationToken)) {
            log.warn("Invalid or expired token: {}", token);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or expired token");
            return;
        }

        userService.enableUser(verificationToken.getUser());
        log.info("User verified: {}", verificationToken.getUser().getEmail());
        response.sendRedirect("http://localhost:3000/login"); // Frontend login URL
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(authentication);
            return ResponseEntity.ok(new AuthResponse(jwt));
        } catch (BadCredentialsException e) {
            log.warn("Bad credentials for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (DisabledException e) {
            log.warn("Login attempt with unverified email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email not verified yet");
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed");
        }
    }
}
