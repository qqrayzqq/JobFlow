package com.jobflow.jobservice.controller;

import com.jobflow.jobservice.domain.Job;
import com.jobflow.jobservice.domain.enums.JobStatus;
import com.jobflow.jobservice.dto.job.UpdateJobDto;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.security.JwtService;
import com.jobflow.jobservice.security.UserDetailsServiceImpl;
import com.jobflow.jobservice.service.JobService;
import com.jobflow.jobservice.service.ViewCounterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private ViewCounterService viewCounterService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void getAllSkills_returns200WithSkills() throws Exception {
        when(jobService.getAllSkills()).thenReturn(Set.of("Java", "Spring"));

        mockMvc.perform(get("/api/jobs/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getJobById_existing_returns200() throws Exception {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("Backend Dev");
        when(jobService.getJobById(1L)).thenReturn(job);

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Backend Dev"));
    }

    @Test
    void getJobById_notFound_returns404() throws Exception {
        when(jobService.getJobById(1L)).thenThrow(new ResourceNotFoundException("Job not found"));

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createJob_validBody_returns201() throws Exception {
        Job saved = new Job();
        saved.setId(1L);
        saved.setTitle("Backend Dev");
        when(jobService.createJob(any())).thenReturn(saved);

        String body = """
                {
                  "title": "Backend Dev",
                  "description": "desc",
                  "salaryMin": 1000,
                  "salaryMax": 2000,
                  "city": "Praha",
                  "skills": "Java,Spring",
                  "status": "PUBLISHED",
                  "companyId": 1
                }
                """;

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Backend Dev"));
    }

    @Test
    void createJob_invalidBody_returns400() throws Exception {
        String body = """
                {
                  "title": "",
                  "description": "desc",
                  "salaryMin": 1000,
                  "salaryMax": 2000,
                  "city": "Praha",
                  "skills": "Java,Spring",
                  "status": "PUBLISHED",
                  "companyId": 1
                }
                """;

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateJob_notFound_returns404() throws Exception {
        UpdateJobDto dto = new UpdateJobDto("Backend Dev", "Praha", "desc", JobStatus.PUBLISHED, 1000, 2000, "Java,Spring");
        when(jobService.updateJob(eq(1L), any())).thenThrow(new ResourceNotFoundException("Job not found"));

        mockMvc.perform(put("/api/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteJob_returns204() throws Exception {
        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isNoContent());
    }
}
