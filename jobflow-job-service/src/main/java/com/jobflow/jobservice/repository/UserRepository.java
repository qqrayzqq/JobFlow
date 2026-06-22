package com.jobflow.jobservice.repository;

import com.jobflow.jobservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final DSLContext dsl;

    public Optional<User> findByEmail(String email){
        return Optional.empty();
    }
}
