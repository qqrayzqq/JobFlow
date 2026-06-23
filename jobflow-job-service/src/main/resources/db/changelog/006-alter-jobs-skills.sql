--liquibase formatted sql

--changeset vladyslav:6

ALTER TABLE jobs ALTER COLUMN skills TYPE TEXT USING array_to_string(skills, ',');