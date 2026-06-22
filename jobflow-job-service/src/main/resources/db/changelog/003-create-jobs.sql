--liquibase formatted sql

--changeset vladyslav:3

CREATE TABLE jobs(
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    city VARCHAR(255) NOT NULL,
    salary_min INTEGER NOT NULL,
    salary_max INTEGER NOT NULL,
    skills TEXT[],
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    company_id BIGINT NOT NULL REFERENCES companies(id)
);