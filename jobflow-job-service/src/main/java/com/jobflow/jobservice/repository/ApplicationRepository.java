package com.jobflow.jobservice.repository;

import com.jobflow.jobservice.domain.Application;
import com.jobflow.jobservice.domain.enums.ApplicationStatus;
import com.jobflow.jobservice.jooq.Tables;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ApplicationRepository {
    private final DSLContext dsl;

    public Application save(Application application) {
        return dsl.insertInto(Tables.APPLICATIONS)
                .set(Tables.APPLICATIONS.CANDIDATE_ID, application.getCandidateId())
                .set(Tables.APPLICATIONS.JOB_ID, application.getJobId())
                .set(Tables.APPLICATIONS.STATUS, application.getStatus().name())
                .returning()
                .fetchOneInto(Application.class);
    }

    public Application updateStatus(Long id, ApplicationStatus status) {
        return dsl.update(Tables.APPLICATIONS)
                .set(Tables.APPLICATIONS.STATUS, status.name())
                .where(Tables.APPLICATIONS.ID.eq(id))
                .returning()
                .fetchOneInto(Application.class);
    }

    public Optional<Application> findById(Long id) {
        return dsl.selectFrom(Tables.APPLICATIONS)
                .where(Tables.APPLICATIONS.ID.eq(id))
                .fetchOptionalInto(Application.class);
    }

    public Optional<Application> findByJobIdAndCandidateId(Long jobId, Long candidateId) {
        return dsl.selectFrom(Tables.APPLICATIONS)
                .where(Tables.APPLICATIONS.JOB_ID.eq(jobId))
                .and(Tables.APPLICATIONS.CANDIDATE_ID.eq(candidateId))
                .fetchOptionalInto(Application.class);
    }

    public List<Application> findByJobId(Long jobId) {
        return dsl.selectFrom(Tables.APPLICATIONS)
                .where(Tables.APPLICATIONS.JOB_ID.eq(jobId))
                .fetchInto(Application.class);
    }

    public List<Application> findByCandidateId(Long candidateId) {
        return dsl.selectFrom(Tables.APPLICATIONS)
                .where(Tables.APPLICATIONS.CANDIDATE_ID.eq(candidateId))
                .fetchInto(Application.class);
    }

    public void delete(Long id) {
        dsl.deleteFrom(Tables.APPLICATIONS)
                .where(Tables.APPLICATIONS.ID.eq(id))
                .execute();
    }
}
