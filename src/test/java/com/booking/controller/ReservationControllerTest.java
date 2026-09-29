package com.booking.controller;

import com.booking.dto.ReservationRequest;
import com.booking.model.*;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User testUser;
    private User adminUser;
    private Resource testResource;
    private Resource unavailableResource;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        resourceRepository.deleteAll();

        testUser = userRepository.findByEmail("user@booking.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("user@booking.com")
                        .password(passwordEncoder.encode("User@123"))
                        .role(Role.USER)
                        .build()));

        adminUser = userRepository.findByEmail("admin@booking.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("admin@booking.com")
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(Role.ADMIN)
                        .build()));

        testResource = resourceRepository.save(Resource.builder()
                .name("Conference Room A")
                .description("Large meeting room")
                .available(true)
                .build());

        unavailableResource = resourceRepository.save(Resource.builder()
                .name("Maintenance Van")
                .description("In repair")
                .available(false)
                .build());

        testReservation = reservationRepository.save(Reservation.builder()
                .user(testUser)
                .resource(testResource)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(BigDecimal.valueOf(150.00))
                .status(Status.PENDING)
                .build());
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testCreateReservationSuccess() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.plusHours(2);
        ReservationRequest request = new ReservationRequest(testResource.getId(), start, end, BigDecimal.valueOf(200.00));

        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resourceName").value("Conference Room A"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testCreateReservationEndTimeBeforeStartTime() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.minusHours(1);
        ReservationRequest request = new ReservationRequest(testResource.getId(), start, end, BigDecimal.valueOf(200.00));

        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("End time must be after start time"));
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testCreateReservationResourceUnavailable() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.plusHours(2);
        ReservationRequest request = new ReservationRequest(unavailableResource.getId(), start, end, BigDecimal.valueOf(200.00));

        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Resource is currently unavailable for booking"));
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testCreateReservationResourceNotFound() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.plusHours(2);
        ReservationRequest request = new ReservationRequest(9999L, start, end, BigDecimal.valueOf(200.00));

        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testGetReservationsAsUser() throws Exception {
        mockMvc.perform(get("/reservations")
                .param("status", "PENDING")
                .param("minPrice", "50")
                .param("maxPrice", "500")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "price")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(username = "admin@booking.com", roles = {"ADMIN"})
    void testGetReservationsAsAdmin() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testGetReservationByIdAsOwner() throws Exception {
        mockMvc.perform(get("/reservations/" + testReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testReservation.getId()));
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testGetReservationByIdNotFound() throws Exception {
        mockMvc.perform(get("/reservations/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "other_user@booking.com", roles = {"USER"})
    void testGetReservationByIdAsOtherUserForbidden() throws Exception {
        userRepository.save(User.builder()
                .email("other_user@booking.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(Role.USER)
                .build());

        mockMvc.perform(get("/reservations/" + testReservation.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@booking.com", roles = {"ADMIN"})
    void testGetReservationByIdAsAdmin() throws Exception {
        mockMvc.perform(get("/reservations/" + testReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testReservation.getId()));
    }

    @Test
    @WithMockUser(username = "admin@booking.com", roles = {"ADMIN"})
    void testUpdateReservationStatusSuccess() throws Exception {
        mockMvc.perform(put("/reservations/" + testReservation.getId() + "/status")
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "admin@booking.com", roles = {"ADMIN"})
    void testUpdateReservationStatusNotFound() throws Exception {
        mockMvc.perform(put("/reservations/9999/status")
                .param("status", "CONFIRMED"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testUpdateReservationStatusAsUserForbidden() throws Exception {
        mockMvc.perform(put("/reservations/" + testReservation.getId() + "/status")
                .param("status", "CONFIRMED"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testUpdateReservationDetailsAsOwnerSuccess() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = start.plusHours(3);
        ReservationRequest request = new ReservationRequest(testResource.getId(), start, end, BigDecimal.valueOf(250.00));

        mockMvc.perform(put("/reservations/" + testReservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(250.00));
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testUpdateReservationDetailsInvalidTime() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = start.minusHours(1);
        ReservationRequest request = new ReservationRequest(testResource.getId(), start, end, BigDecimal.valueOf(250.00));

        mockMvc.perform(put("/reservations/" + testReservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "other_user@booking.com", roles = {"USER"})
    void testUpdateReservationDetailsAsOtherUserForbidden() throws Exception {
        userRepository.save(User.builder()
                .email("other_user@booking.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(Role.USER)
                .build());

        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = start.plusHours(3);
        ReservationRequest request = new ReservationRequest(testResource.getId(), start, end, BigDecimal.valueOf(250.00));

        mockMvc.perform(put("/reservations/" + testReservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testUpdateReservationDetailsNotFound() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = start.plusHours(3);
        ReservationRequest request = new ReservationRequest(testResource.getId(), start, end, BigDecimal.valueOf(250.00));

        mockMvc.perform(put("/reservations/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@booking.com", roles = {"ADMIN"})
    void testDeleteReservationAsAdminSuccess() throws Exception {
        mockMvc.perform(delete("/reservations/" + testReservation.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@booking.com", roles = {"ADMIN"})
    void testDeleteReservationNotFound() throws Exception {
        mockMvc.perform(delete("/reservations/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@booking.com", roles = {"USER"})
    void testDeleteReservationAsUserForbidden() throws Exception {
        mockMvc.perform(delete("/reservations/" + testReservation.getId()))
                .andExpect(status().isForbidden());
    }
}
