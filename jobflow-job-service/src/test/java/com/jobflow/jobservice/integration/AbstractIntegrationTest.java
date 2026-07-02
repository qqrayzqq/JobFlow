package com.jobflow.jobservice.integration;

import com.jobflow.jobservice.elasticsearch.JobDocument;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @ServiceConnection(name = "redis")
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @ServiceConnection
    static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.6.0");

    @ServiceConnection
    static final ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer(DockerImageName.parse("elasticsearch:9.0.3"))
                    .withEnv("xpack.security.enabled", "false");

    static {
        postgres.start();
        redis.start();
        kafka.start();
        elasticsearch.start();
    }

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @BeforeEach
    void cleanState() {
        dslContext.execute("TRUNCATE TABLE users, companies, jobs, applications, subscriptions RESTART IDENTITY CASCADE");

        IndexOperations jobIndex = elasticsearchOperations.indexOps(JobDocument.class);
        if (jobIndex.exists()) jobIndex.delete();
    }
}
