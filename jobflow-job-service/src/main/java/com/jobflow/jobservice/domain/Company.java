package com.jobflow.jobservice.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Company {
    @EqualsAndHashCode.Include
    private Long id;

    private String name;

    private String description;

    private String city;

    private Long userId;

    public Company(String name, String city, String description, Long userId) {
        this.name = name;
        this.city = city;
        this.description = description;
        this.userId = userId;
    }
}
