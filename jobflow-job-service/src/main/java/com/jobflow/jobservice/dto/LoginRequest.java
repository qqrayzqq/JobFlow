package com.jobflow.jobservice.dto;

public record LoginRequest(
        String email,
        String password
) {
}
