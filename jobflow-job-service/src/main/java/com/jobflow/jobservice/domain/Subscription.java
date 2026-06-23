package com.jobflow.jobservice.domain;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Subscription {
    @EqualsAndHashCode.Include
    private Long id;

    private String skill;

    private Long userId;

    public Subscription(Long userId, String skill) {
        this.userId = userId;
        this.skill = skill;
    }
}
