package com.example.minorapp.controller;

import com.example.minorapp.dto.AuthResponse;
import com.example.minorapp.dto.ApiResponse;
import com.example.minorapp.dto.ChangePasswordRequest;
import com.example.minorapp.dto.LoginRequest;
import com.example.minorapp.dto.RegisterRequest;
import com.example.minorapp.dto.VerifyIdentityRequest;
import com.example.minorapp.service.AuthService;
import com.example.minorapp.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (ResponseStatusException ex) {
            AuthResponse failure = new AuthResponse("FAILURE", ex.getReason(), null);
            return ResponseEntity.status(ex.getStatusCode()).body(failure);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
        } catch (ResponseStatusException ex) {
            AuthResponse failure = new AuthResponse("FAILURE", ex.getReason(), null);
            return ResponseEntity.status(ex.getStatusCode()).body(failure);
        }
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<ApiResponse<Object>> verifyIdentity(@Valid @RequestBody VerifyIdentityRequest request) {
        try {
            authService.verifyIdentity(request);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Identity verified.", null));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                .body(new ApiResponse<>("FAILURE", ex.getReason(), null));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        HttpServletRequest servletRequest
    ) {
        try {
            String authHeader = servletRequest.getHeader("Authorization");
            String authenticatedEmail = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.isAccessTokenValid(token)) {
                    authenticatedEmail = jwtUtil.extractEmail(token);
                }
            }

            authService.changePassword(request, authenticatedEmail);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Password updated successfully.", null));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                .body(new ApiResponse<>("FAILURE", ex.getReason(), null));
        }
    }
}




