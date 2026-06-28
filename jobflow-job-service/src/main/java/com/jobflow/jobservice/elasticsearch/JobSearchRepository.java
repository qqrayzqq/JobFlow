package com.jobflow.jobservice.elasticsearch;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface JobSearchRepository extends ElasticsearchRepository<JobDocument, Long> {
    @Query("""
        { "multi_match": { "query": "?0", "fields": ["title", "description", "skills"] } }
        """)
    List<JobDocument> search(String text);
}
