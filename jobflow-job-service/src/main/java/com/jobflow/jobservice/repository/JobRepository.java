package com.jobflow.jobservice.repository;

import com.jobflow.jobservice.domain.Job;
import com.jobflow.jobservice.domain.enums.JobStatus;
import com.jobflow.jobservice.jooq.Tables;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JobRepository {
    private final DSLContext dsl;

    public Job save(Job job) {
        return dsl.insertInto(Tables.JOBS)
                .set(Tables.JOBS.CITY, job.getCity())
                .set(Tables.JOBS.COMPANY_ID, job.getCompanyId())
                .set(Tables.JOBS.DESCRIPTION, job.getDescription())
                .set(Tables.JOBS.SALARY_MAX, job.getSalaryMax())
                .set(Tables.JOBS.SALARY_MIN, job.getSalaryMin())
                .set(Tables.JOBS.SKILLS, job.getSkills())
                .set(Tables.JOBS.STATUS, job.getStatus().name())
                .set(Tables.JOBS.TITLE, job.getTitle())
                .returning()
                .fetchOneInto(Job.class);
    }

    public Job update(Job job) {
        return dsl.update(Tables.JOBS)
                .set(Tables.JOBS.CITY, job.getCity())
                .set(Tables.JOBS.TITLE, job.getTitle())
                .set(Tables.JOBS.STATUS, job.getStatus().name())
                .set(Tables.JOBS.DESCRIPTION, job.getDescription())
                .set(Tables.JOBS.SALARY_MIN, job.getSalaryMin())
                .set(Tables.JOBS.SALARY_MAX, job.getSalaryMax())
                .set(Tables.JOBS.SKILLS, job.getSkills())
                .where(Tables.JOBS.ID.eq(job.getId()))
                .returning()
                .fetchOneInto(Job.class);
    }

    public void delete(Long id) {
        dsl.deleteFrom(Tables.JOBS)
                .where(Tables.JOBS.ID.eq(id))
                .execute();
    }

    public Optional<Job> findById(Long id) {
        return dsl.selectFrom(Tables.JOBS)
                .where(Tables.JOBS.ID.eq(id))
                .fetchOptionalInto(Job.class);
    }

    public List<Job> findByCompanyId(Long companyId) {
        return dsl.selectFrom(Tables.JOBS)
                .where(Tables.JOBS.COMPANY_ID.eq(companyId))
                .fetchInto(Job.class);
    }

    public List<Job> findByStatus(JobStatus status) {
        return dsl.selectFrom(Tables.JOBS)
                .where(Tables.JOBS.STATUS.eq(status.name()))
                .fetchInto(Job.class);
    }

    public List<Job> findBySalaryRange(Integer min, Integer max) {
        return dsl.selectFrom(Tables.JOBS)
                .where(Tables.JOBS.SALARY_MIN.ge(min)).and(Tables.JOBS.SALARY_MAX.le(max))
                .fetchInto(Job.class);
    }

    public List<Job> findByCity(String city) {
        return dsl.selectFrom(Tables.JOBS)
                .where(Tables.JOBS.CITY.eq(city))
                .fetchInto(Job.class);
    }
}