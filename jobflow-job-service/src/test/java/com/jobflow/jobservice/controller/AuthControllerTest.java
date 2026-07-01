package com.jobflow.jobservice.controller;

import com.jobflow.jobservice.domain.enums.UserRole;
import com.jobflow.jobservice.dto.auth.AuthResponse;
import com.jobflow.jobservice.dto.auth.LoginRequest;
import com.jobflow.jobservice.dto.auth.RegisterRequest;
import com.jobflow.jobservice.exception.DuplicateResourceException;
import com.jobflow.jobservice.security.JwtService;
import com.jobflow.jobservice.security.UserDetailsServiceImpl;
import com.jobflow.jobservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void register_validBody_returns201() throws Exception {
        RegisterRequest dto = new RegisterRequest("tester", "test@gmail.com", "testpass", UserRole.CANDIDATE);
        when(authService.register(any())).thenReturn(new AuthResponse("token"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void register_invalidBody_returns400() throws Exception {
        String body = """
                {
                  "name": "tester",
                  "email": "",
                  "password": "testpass",
                  "role": "CANDIDATE"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest dto = new RegisterRequest("tester", "test@gmail.com", "testpass", UserRole.CANDIDATE);
        when(authService.register(any())).thenThrow(new DuplicateResourceException("Email already taken"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_validBody_returns200() throws Exception {
        LoginRequest dto = new LoginRequest("test@gmail.com", "testpass");
        when(authService.login(any())).thenReturn(new AuthResponse("token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void login_invalidBody_returns400() throws Exception {
        String body = """
                {
                  "email": "",
                  "password": "testpass"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
