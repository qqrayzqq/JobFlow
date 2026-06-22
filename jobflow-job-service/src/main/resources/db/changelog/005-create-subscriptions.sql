--liquibase formatted sql

--changeset vladyslav:5

CREATE TABLE subscriptions(
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL REFERENCES users(id),
                              skill VARCHAR(255) NOT NULL
);