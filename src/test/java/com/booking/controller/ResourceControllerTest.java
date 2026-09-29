package com.booking.controller;

import com.booking.dto.ResourceRequest;
import com.booking.model.Resource;
import com.booking.repository.ResourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceRepository resourceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Resource testResource;

    @BeforeEach
    void setUp() {
        resourceRepository.deleteAll();
        testResource = Resource.builder()
                .name("Conference Room A")
                .description("Large meeting room")
                .available(true)
                .build();
        testResource = resourceRepository.save(testResource);
    }

    @Test
    @WithMockUser(username = "admin@booking.com", roles = {"ADMIN"})
    void testCreateResourceAsAdminSuccess() throws Exception {
        ResourceRequest request = new ResourceRequest("Projector X", "4K HD Projector", true);

        mockMvc.perform(post("/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Projector X"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testCreateResourceAsUserForbidden() throws Exception {
        ResourceRequest request = new ResourceRequest("Projector X", "4K HD Projector", true);

        mockMvc.perform(post("/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testGetAllResourcesSuccess() throws Exception {
        mockMvc.perform(get("/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Conference Room A"));
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testGetResourceByIdSuccess() throws Exception {
        mockMvc.perform(get("/resources/" + testResource.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conference Room A"));
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testGetResourceByIdNotFound() throws Exception {
        mockMvc.perform(get("/resources/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@booking.com", roles = {"ADMIN"})
    void testUpdateResourceAsAdminSuccess() throws Exception {
        ResourceRequest request = new ResourceRequest("Updated Room Name", "Updated Description", false);

        mockMvc.perform(put("/resources/" + testResource.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Room Name"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @WithMockUser(username = "admin@booking.com", roles = {"ADMIN"})
    void testUpdateResourceNotFound() throws Exception {
        ResourceRequest request = new ResourceRequest("Name", "Desc", true);

        mockMvc.perform(put("/resources/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testUpdateResourceAsUserForbidden() throws Exception {
        ResourceRequest request = new ResourceRequest("Updated Room Name", "Updated Description", false);

        mockMvc.perform(put("/resources/" + testResource.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@booking.com", roles = {"ADMIN"})
    void testDeleteResourceAsAdminSuccess() throws Exception {
        mockMvc.perform(delete("/resources/" + testResource.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@booking.com", roles = {"ADMIN"})
    void testDeleteResourceNotFound() throws Exception {
        mockMvc.perform(delete("/resources/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testDeleteResourceAsUserForbidden() throws Exception {
        mockMvc.perform(delete("/resources/" + testResource.getId()))
                .andExpect(status().isForbidden());
    }
}
