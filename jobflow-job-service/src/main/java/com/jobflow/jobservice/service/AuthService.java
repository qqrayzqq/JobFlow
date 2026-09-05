package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.User;
import com.jobflow.jobservice.domain.enums.UserRole;
import com.jobflow.jobservice.dto.auth.AuthResponse;
import com.jobflow.jobservice.dto.auth.LoginRequest;
import com.jobflow.jobservice.dto.auth.RegisterRequest;
import com.jobflow.jobservice.exception.DuplicateResourceException;
import com.jobflow.jobservice.exception.RateLimitExceededException;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.UserRepository;
import com.jobflow.jobservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RateLimiterService rateLimiterService;

    @Transactional
    public AuthResponse register(RegisterRequest dto) {
        if (dto.role() == UserRole.ADMIN) throw new IllegalArgumentException("Role ADMIN is not allowed for registration");
        if (userRepository.findByEmail(dto.email()).isPresent()) throw new DuplicateResourceException("Email already taken");

        User user = new User(dto.email(), dto.name(), passwordEncoder.encode(dto.password()), dto.role());
        User savedUser = userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(savedUser));
    }

    public AuthResponse login(LoginRequest dto) {
        if(!rateLimiterService.tryAcquire("ratelimit:login:" + dto.email().toLowerCase(), 5, Duration.ofMinutes(15))){
            throw new RateLimitExceededException("Too many requests for login, try again later");
        }
        User user = userRepository.findByEmail(dto.email()).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist"));
        if (!passwordEncoder.matches(dto.password(), user.getPassword()))
            throw new IllegalArgumentException("Wrong email or password");
        return new AuthResponse(jwtService.generateToken(user));
    }
}
