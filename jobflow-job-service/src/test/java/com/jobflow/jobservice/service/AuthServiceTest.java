package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.User;
import com.jobflow.jobservice.domain.enums.UserRole;
import com.jobflow.jobservice.dto.auth.AuthResponse;
import com.jobflow.jobservice.dto.auth.LoginRequest;
import com.jobflow.jobservice.dto.auth.RegisterRequest;
import com.jobflow.jobservice.exception.DuplicateResourceException;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobflow.jobservice.exception.RateLimitExceededException;
import com.jobflow.jobservice.repository.UserRepository;
import com.jobflow.jobservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RateLimiterService rateLimiterService;

    private AuthService authService;

    @BeforeEach
    void setUp(){
        authService = new AuthService(userRepository, passwordEncoder, jwtService, rateLimiterService);
    }

    @Test
    void register_adminRole_throwsIllegalArgumentException() {
        RegisterRequest dto = new RegisterRequest(
                "tester",
                "test@gmail.com",
                "testpass",
                UserRole.ADMIN
        );

        assertThatThrownBy(() -> authService.register(dto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest dto = new RegisterRequest(
                "tester",
                "test@gmail.com",
                "testpass",
                UserRole.CANDIDATE
        );

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(dto)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void register_success_returnsToken() {
        RegisterRequest dto = new RegisterRequest(
                "tester",
                "test@gmail.com",
                "testpass",
                UserRole.CANDIDATE
        );

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(jwtService.generateToken(dto.email())).thenReturn("token");

        AuthResponse response = authService.register(dto);

        assertThat(response.token()).isEqualTo("token");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(dto.password());
    }

    @Test
    void login_rateLimitExceeded_throwsRateLimitExceededException() {
        when(rateLimiterService.tryAcquire("ratelimit:login:test@gmail.com", 5, Duration.ofMinutes(15))).thenReturn(false);

        LoginRequest dto = new LoginRequest(
                "test@gmail.com",
                "testpass"
        );

        assertThatThrownBy(() -> authService.login(dto)).isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void login_wrongPassword_throwsIllegalArgumentException() {
        when(rateLimiterService.tryAcquire("ratelimit:login:test@gmail.com", 5, Duration.ofMinutes(15))).thenReturn(true);

        LoginRequest dto = new LoginRequest(
                "test@gmail.com",
                "testpass"
        );

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(new User("test@gmail.com", "tester", "hashed", UserRole.CANDIDATE)));
        when(passwordEncoder.matches("testpass", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void login_success_returnsToken() {
        when(rateLimiterService.tryAcquire("ratelimit:login:test@gmail.com", 5, Duration.ofMinutes(15))).thenReturn(true);

        LoginRequest dto = new LoginRequest(
                "test@gmail.com",
                "testpass"
        );

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(new User("test@gmail.com", "tester", "hashed", UserRole.CANDIDATE)));
        when(jwtService.generateToken(dto.email())).thenReturn("token");
        when(passwordEncoder.matches("testpass", "hashed")).thenReturn(true);

        AuthResponse response = authService.login(dto);
        assertThat(response.token()).isEqualTo("token");
    }
}
