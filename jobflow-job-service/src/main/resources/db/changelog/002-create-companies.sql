--liquibase formatted sql

--changeset vladyslav:2

CREATE TABLE companies
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    city        VARCHAR(255) NOT NULL,
    user_id     BIGINT       NOT NULL REFERENCES users(id)
);