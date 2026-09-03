package com.jobflow.jobservice.security;

import com.jobflow.jobservice.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class UserDetailsPrincipal implements UserDetails {
    private final String email;
    private final String password;
    private final String role;
    private final Long id;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public Long getId(){return id;}

    public String getRole(){return role;}

    @Override
    public boolean isEnabled() {
        return true;
    }
}
