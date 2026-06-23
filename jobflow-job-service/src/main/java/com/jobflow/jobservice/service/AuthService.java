package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.User;
import com.jobflow.jobservice.domain.enums.UserRole;
import com.jobflow.jobservice.dto.auth.AuthResponse;
import com.jobflow.jobservice.dto.auth.LoginRequest;
import com.jobflow.jobservice.dto.auth.RegisterRequest;
import com.jobflow.jobservice.exception.DuplicateResourceException;
import com.jobflow.jobservice.repository.UserRepository;
import com.jobflow.jobservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest dto) {
        if (dto.role() == UserRole.ADMIN) throw new IllegalArgumentException("Role ADMIN is not allowed for registration");
        if (userRepository.findByEmail(dto.email()).isPresent()) throw new DuplicateResourceException("Email already taken");

        User user = new User(dto.email(), dto.name(), passwordEncoder.encode(dto.password()), dto.role());
        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(dto.email()));
    }

    public AuthResponse login(LoginRequest dto) {
        var user = userRepository.findByEmail(dto.email());
        if (user.isEmpty() || !passwordEncoder.matches(dto.password(), user.get().getPassword()))
            throw new IllegalArgumentException("Wrong email or password");
        return new AuthResponse(jwtService.generateToken(dto.email()));
    }
}
