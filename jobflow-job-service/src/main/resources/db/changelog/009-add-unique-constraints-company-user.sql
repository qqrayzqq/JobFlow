--liquibase formatted sql

--changeset vladyslav:9

ALTER TABLE companies ADD CONSTRAINT uq_companies_user_owner UNIQUE (user_id);