package com.jobflow.jobservice.repository;

import com.jobflow.jobservice.domain.User;
import com.jobflow.jobservice.jooq.Tables;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final DSLContext dsl;

    public Optional<User> findByEmail(String email){
        return dsl.selectFrom(Tables.USERS)
                .where(Tables.USERS.EMAIL.eq(email))
                .fetchOptionalInto(User.class);
    }

    public Optional<User> findById(Long id) {
        return dsl.selectFrom(Tables.USERS)
                .where(Tables.USERS.ID.eq(id))
                .fetchOptionalInto(User.class);
    }

    public User save(User user){
        return dsl.insertInto(Tables.USERS)
                .set(Tables.USERS.EMAIL, user.getEmail())
                .set(Tables.USERS.NAME, user.getName())
                .set(Tables.USERS.ROLE, user.getRole().name())
                .set(Tables.USERS.PASSWORD, user.getPassword())
                .returning()
                .fetchOneInto(User.class);
    }
}
