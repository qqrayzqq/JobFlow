package com.jobflow.jobservice.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}
