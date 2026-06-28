package com.jobflow.jobservice.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface JobSearchRepository extends ElasticsearchRepository<JobDocument, Long> {
}
