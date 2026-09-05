--liquibase formatted sql

--changeset vladyslav:8

ALTER TABLE applications ADD CONSTRAINT uq_applications_job_candidate UNIQUE (job_id, candidate_id);
ALTER TABLE subscriptions ADD CONSTRAINT uq_subscriptions_user_skill UNIQUE (user_id, skill);
