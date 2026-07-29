package com.jobflow.jobservice.controller;

import com.jobflow.jobservice.domain.Company;
import com.jobflow.jobservice.domain.User;
import com.jobflow.jobservice.domain.enums.UserRole;
import com.jobflow.jobservice.dto.user.UserMeResponse;
import com.jobflow.jobservice.dto.user.UserPublicResponse;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.CompanyRepository;
import com.jobflow.jobservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "Current user and public user lookups")
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Operation(summary = "Get current authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserMeResponse> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long companyId = null;
        if (user.getRole() == UserRole.COMPANY) {
            companyId = companyRepository.findByUserId(user.getId()).map(Company::getId).orElse(null);
        }

        return ResponseEntity.ok(new UserMeResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), companyId));
    }

    @Operation(summary = "Get public user info by ID (name only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserPublicResponse> getPublicUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(new UserPublicResponse(user.getId(), user.getName()));
    }
}
