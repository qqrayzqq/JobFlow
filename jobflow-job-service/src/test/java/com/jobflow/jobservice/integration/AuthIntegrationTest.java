package com.jobflow.jobservice.integration;

import com.jobflow.jobservice.domain.enums.UserRole;
import com.jobflow.jobservice.dto.auth.LoginRequest;
import com.jobflow.jobservice.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_thenLogin_returnsToken() throws Exception {
        RegisterRequest dtoRegister = new RegisterRequest(
                "test",
                "test@gmail.com",
                "testpass",
                UserRole.CANDIDATE
        );

        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dtoRegister)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());

        LoginRequest dtoLogin = new LoginRequest(
                "test@gmail.com",
                "testpass"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest dto = new RegisterRequest(
                "test",
                "test@gmail.com",
                "testpass",
                UserRole.CANDIDATE
        );
        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void login_wrongPassword_returns400() throws Exception {
        RegisterRequest dtoRegister = new RegisterRequest(
                "test",
                "test@gmail.com",
                "testpass",
                UserRole.CANDIDATE
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoRegister)))
                .andExpect(status().isCreated());

        LoginRequest dtoLogin = new LoginRequest(
                "test@gmail.com",
                "wrongpass"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoLogin)))
                .andExpect(status().isBadRequest());
    }
}
