package com.jobflow.jobservice.integration;

import com.jobflow.jobservice.domain.enums.JobStatus;
import com.jobflow.jobservice.domain.enums.UserRole;
import com.jobflow.jobservice.dto.company.CreateCompanyDto;
import com.jobflow.jobservice.dto.job.CreateJobDto;
import com.jobflow.jobservice.dto.auth.RegisterRequest;
import com.jobflow.jobservice.elasticsearch.JobDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JobIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private final CreateJobDto jobPayload = new CreateJobDto(
            "Java Developer", "Backend role", 40000, 80000,
            "Praha", "Java,Spring", JobStatus.PUBLISHED, 1L);

    private String registerAndGetToken(String email, UserRole role) throws Exception {
        RegisterRequest dto = new RegisterRequest("tester", email, "testpass", role);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }

    private long createCompany(String token) throws Exception {
        CreateCompanyDto company = new CreateCompanyDto("Acme", "IT company", "Praha", 1L);

        MvcResult result = mockMvc.perform(post("/api/companies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(company)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void createJob_noToken_returns403() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobPayload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createJob_asCandidate_returns403() throws Exception {
        String token = registerAndGetToken("candidate@test.cz", UserRole.CANDIDATE);

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobPayload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createJob_asCompany_returns201() throws Exception {
        String token = registerAndGetToken("company@test.cz", UserRole.COMPANY);
        long companyId = createCompany(token);

        CreateJobDto job = new CreateJobDto(
                "Java Developer", "Backend role", 40000, 80000,
                "Praha", "Java,Spring", JobStatus.PUBLISHED, companyId);

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Java Developer"));
    }

    @Test
    void search_findsIndexedJob_returns200() throws Exception {
        String token = registerAndGetToken("company@test.cz", UserRole.COMPANY);
        long companyId = createCompany(token);

        CreateJobDto job = new CreateJobDto(
                "Java Developer", "Backend role", 40000, 80000,
                "Praha", "Java,Spring", JobStatus.PUBLISHED, companyId);
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isCreated());

        elasticsearchOperations.indexOps(JobDocument.class).refresh();

        mockMvc.perform(get("/api/jobs/search").param("q", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Java Developer"));
    }
}
