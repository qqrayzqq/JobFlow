package com.jobflow.jobservice.repository;

import com.jobflow.jobservice.domain.Subscription;
import com.jobflow.jobservice.jooq.Tables;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubscriptionRepository {
    private final DSLContext dsl;

    public Subscription save(Subscription subscription) {
        return dsl.insertInto(Tables.SUBSCRIPTIONS)
                .set(Tables.SUBSCRIPTIONS.USER_ID, subscription.getUserId())
                .set(Tables.SUBSCRIPTIONS.SKILL, subscription.getSkill())
                .returning()
                .fetchOneInto(Subscription.class);
    }

    public void delete(Long id) {
        dsl.deleteFrom(Tables.SUBSCRIPTIONS)
                .where(Tables.SUBSCRIPTIONS.ID.eq(id))
                .execute();
    }

    public Optional<Subscription> findById(Long id) {
        return dsl.selectFrom(Tables.SUBSCRIPTIONS)
                .where(Tables.SUBSCRIPTIONS.ID.eq(id))
                .fetchOptionalInto(Subscription.class);
    }

    public Optional<Subscription> findByUserIdAndSkill(Long userId, String skill) {
        return dsl.selectFrom(Tables.SUBSCRIPTIONS)
                .where(Tables.SUBSCRIPTIONS.USER_ID.eq(userId))
                .and(Tables.SUBSCRIPTIONS.SKILL.eq(skill))
                .fetchOptionalInto(Subscription.class);
    }

    public List<Subscription> findByUserId(Long userId) {
        return dsl.selectFrom(Tables.SUBSCRIPTIONS)
                .where(Tables.SUBSCRIPTIONS.USER_ID.eq(userId))
                .fetchInto(Subscription.class);
    }
}
