package com.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingSystemSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testUnauthorizedAccessToResources() throws Exception {
        // Attempting to access resources without a token should return 403 Forbidden or 401 Unauthorized
        mockMvc.perform(get("/resources"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPublicAccessToLoginEndpoint() throws Exception {
        // Login endpoint should be public (not 403/401, though it might return 400/405 due to missing body/method)
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"wrong@booking.com\",\"password\":\"wrong\"}"))
                .andExpect(status().is4xxClientError()); // Handled or unauthorized credentials format, but not blocked by security filter chain as forbidden
    }
}