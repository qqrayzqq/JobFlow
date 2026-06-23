package com.jobflow.jobservice.repository;

import com.jobflow.jobservice.domain.Company;
import com.jobflow.jobservice.jooq.Tables;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CompanyRepository {
    private final DSLContext dsl;

    public Company save(Company company) {
        return dsl.insertInto(Tables.COMPANIES)
                .set(Tables.COMPANIES.CITY, company.getCity())
                .set(Tables.COMPANIES.DESCRIPTION, company.getDescription())
                .set(Tables.COMPANIES.NAME, company.getName())
                .set(Tables.COMPANIES.USER_ID, company.getUserId())
                .returning()
                .fetchOneInto(Company.class);
    }

    public Company update(Company company) {
        return dsl.update(Tables.COMPANIES)
                .set(Tables.COMPANIES.CITY, company.getCity())
                .set(Tables.COMPANIES.DESCRIPTION, company.getDescription())
                .set(Tables.COMPANIES.NAME, company.getName())
                .set(Tables.COMPANIES.USER_ID, company.getUserId())
                .where(Tables.COMPANIES.ID.eq(company.getId()))
                .returning()
                .fetchOneInto(Company.class);
    }

    public void delete(Long id) {
        dsl.delete(Tables.COMPANIES)
                .where(Tables.COMPANIES.ID.eq(id))
                .execute();
    }

    public Optional<Company> findById(Long id) {
        return dsl.selectFrom(Tables.COMPANIES)
                .where(Tables.COMPANIES.ID.eq(id))
                .fetchOptionalInto(Company.class);
    }

    public Optional<Company> findByName(String name) {
        return dsl.selectFrom(Tables.COMPANIES)
                .where(Tables.COMPANIES.NAME.eq(name))
                .fetchOptionalInto(Company.class);
    }

    public List<Company> findByCities(List<String> cities) {
        return dsl.selectFrom(Tables.COMPANIES)
                .where(Tables.COMPANIES.CITY.in(cities))
                .fetchInto(Company.class);
    }
}