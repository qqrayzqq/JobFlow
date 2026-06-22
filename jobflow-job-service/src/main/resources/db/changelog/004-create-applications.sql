--liquibase formatted sql

--changeset vladyslav:4

CREATE TABLE applications(
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    job_id BIGINT NOT NULL  REFERENCES jobs(id),
    candidate_id BIGINT NOT NULL REFERENCES users(id)
);