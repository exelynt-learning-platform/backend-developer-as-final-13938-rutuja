package com.booking.controller;

import com.booking.dto.AuthRequest;
import com.booking.dto.AuthResponse;
import com.booking.model.Role;
import com.booking.model.User;
import com.booking.repository.UserRepository;
import com.booking.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail("auth_test@booking.com").isEmpty()) {
            User user = User.builder()
                    .email("auth_test@booking.com")
                    .password(passwordEncoder.encode("Test@123"))
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
        }
    }

    @Test
    void testLoginSuccess() throws Exception {
        AuthRequest request = new AuthRequest("auth_test@booking.com", "Test@123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        AuthRequest request = new AuthRequest("auth_test@booking.com", "WrongPassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginUserNotFound() throws Exception {
        AuthRequest request = new AuthRequest("nonexistent@booking.com", "Password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
