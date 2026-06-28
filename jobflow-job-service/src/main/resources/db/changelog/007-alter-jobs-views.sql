--liquibase formatted sql

--changeset vladyslav:7

ALTER TABLE jobs ADD COLUMN views BIGINT NOT NULL DEFAULT 0;