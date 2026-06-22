package com.jobflow.jobservice.domain;

import com.jobflow.jobservice.domain.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @EqualsAndHashCode.Include
    private Long id;

    private String name;

    private String email;

    private String password;

    private UserRole role;

    private LocalDateTime createdAt;
}
