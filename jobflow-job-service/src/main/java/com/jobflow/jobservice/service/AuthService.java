package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.User;
import com.jobflow.jobservice.domain.enums.UserRole;
import com.jobflow.jobservice.dto.AuthResponse;
import com.jobflow.jobservice.dto.LoginRequest;
import com.jobflow.jobservice.dto.RegisterRequest;
import com.jobflow.jobservice.repository.UserRepository;
import com.jobflow.jobservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest dto){
        String email = dto.email();
        UserRole role = dto.role();
        String name = dto.name();
        String password = passwordEncoder.encode(dto.password());

        if(role == UserRole.ADMIN) throw new IllegalArgumentException("bad role");
        if(userRepository.findByEmail(email).isPresent()) throw new IllegalArgumentException("email already taken");

        User user = new User(email, name, password, role);
        userRepository.save(user);
        String token = jwtService.generateToken(email);
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest dto){
        String email = dto.email();
        var user = userRepository.findByEmail(email);
        if(user.isEmpty() || !passwordEncoder.matches(dto.password(), user.get().getPassword())) throw new IllegalArgumentException("wrong login or password");
        return new AuthResponse(jwtService.generateToken(email));
    }
}
