package com.jobflow.jobservice.controller;

import com.jobflow.jobservice.dto.auth.AuthResponse;
import com.jobflow.jobservice.dto.auth.LoginRequest;
import com.jobflow.jobservice.dto.auth.RegisterRequest;
import com.jobflow.jobservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest dto){
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest dto){
        return ResponseEntity.ok(authService.register(dto));
    }
}
