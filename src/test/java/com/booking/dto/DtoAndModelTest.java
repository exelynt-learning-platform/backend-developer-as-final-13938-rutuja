package com.booking.dto;

import com.booking.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DtoAndModelTest {

    @Test
    void testAuthDTOs() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");
        assertEquals("user@test.com", request.getEmail());
        assertEquals("password", request.getPassword());

        AuthResponse response = new AuthResponse();
        response.setToken("token123");
        response.setRole("ROLE_USER");
        assertEquals("token123", response.getToken());
        assertEquals("ROLE_USER", response.getRole());

        AuthResponse responseParam = new AuthResponse("token456", "ROLE_ADMIN");
        assertEquals("token456", responseParam.getToken());
        assertEquals("ROLE_ADMIN", responseParam.getRole());
    }

    @Test
    void testResourceDTOsAndEntity() {
        ResourceRequest request = ResourceRequest.builder()
                .name("Resource A")
                .description("Desc")
                .available(true)
                .build();
        assertEquals("Resource A", request.getName());
        assertEquals("Desc", request.getDescription());
        assertTrue(request.getAvailable());

        Resource resource = new Resource(1L, "Resource B", "Desc B", true);
        resource.setId(2L);
        resource.setName("Resource C");
        resource.setDescription("Desc C");
        resource.setAvailable(false);

        assertEquals(2L, resource.getId());
        assertEquals("Resource C", resource.getName());
        assertEquals("Desc C", resource.getDescription());
        assertFalse(resource.getAvailable());
        assertNotNull(resource.toString());
    }

    @Test
    void testReservationDTOsAndEntity() {
        LocalDateTime now = LocalDateTime.now();
        ReservationRequest request = new ReservationRequest(1L, now, now.plusHours(2), BigDecimal.valueOf(100));
        assertEquals(1L, request.getResourceId());
        assertEquals(now, request.getStartTime());
        assertEquals(now.plusHours(2), request.getEndTime());
        assertEquals(BigDecimal.valueOf(100), request.getPrice());

        ReservationResponse response = new ReservationResponse(10L, "user@test.com", "Resource X", now, now.plusHours(2), BigDecimal.valueOf(100), "PENDING");
        assertEquals(10L, response.getId());
        assertEquals("user@test.com", response.getUserEmail());
        assertEquals("Resource X", response.getResourceName());
        assertEquals(now, response.getStartTime());
        assertEquals(now.plusHours(2), response.getEndTime());
        assertEquals(BigDecimal.valueOf(100), response.getPrice());
        assertEquals("PENDING", response.getStatus());

        User user = User.builder().id(1L).email("user@test.com").password("pass").role(Role.USER).build();
        Resource res = Resource.builder().id(2L).name("Room 101").description("Room").available(true).build();

        Reservation reservation = new Reservation(100L, user, res, now, now.plusHours(1), BigDecimal.valueOf(50), Status.CONFIRMED);
        assertEquals(100L, reservation.getId());
        assertEquals(user, reservation.getUser());
        assertEquals(res, reservation.getResource());
        assertEquals(Status.CONFIRMED, reservation.getStatus());
        assertNotNull(reservation.toString());
    }

    @Test
    void testUserEntity() {
        User user = new User(1L, "admin@test.com", "pass", Role.ADMIN);
        user.setId(2L);
        user.setEmail("admin2@test.com");
        user.setPassword("newpass");
        user.setRole(Role.USER);

        assertEquals(2L, user.getId());
        assertEquals("admin2@test.com", user.getEmail());
        assertEquals("newpass", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertNotNull(user.toString());
    }

    @Test
    void testEnums() {
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.USER, Role.valueOf("USER"));
        assertEquals(Status.PENDING, Status.valueOf("PENDING"));
        assertEquals(Status.CONFIRMED, Status.valueOf("CONFIRMED"));
        assertEquals(Status.CANCELLED, Status.valueOf("CANCELLED"));
    }
}
