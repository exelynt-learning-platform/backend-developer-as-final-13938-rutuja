package com.booking.controller;

import com.booking.dto.ReservationRequest;
import com.booking.dto.ReservationResponse;
import com.booking.exception.BadRequestException;
import com.booking.exception.ReservationNotFoundException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.exception.UserNotFoundException;
import com.booking.model.*;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationController(ReservationRepository reservationRepository,
                                  ResourceRepository resourceRepository,
                                  UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    // CREATE: USER or ADMIN can create reservation
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().isEqual(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + authentication.getName()));

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));

        if (resource.getAvailable() != null && !resource.getAvailable()) {
            throw new BadRequestException("Resource is currently unavailable for booking");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(request.getPrice())
                .status(Status.PENDING)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
    }

    // READ ALL: ADMIN gets all, USER gets only their own (with filtering, pagination, sorting)
    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getReservations(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + authentication.getName()));

        Long userId = (user.getRole() == Role.ADMIN) ? null : user.getId();

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Reservation> reservations = reservationRepository.findWithFilters(status, minPrice, maxPrice, userId, pageable);

        return ResponseEntity.ok(reservations.map(this::mapToResponse));
    }

    // READ SINGLE: ADMIN can view any, USER can view only their own
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long id,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + authentication.getName()));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));

        if (user.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to view this reservation");
        }

        return ResponseEntity.ok(mapToResponse(reservation));
    }

    // ADMIN: Update reservation status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam Status status) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));

        reservation.setStatus(status);
        Reservation updated = reservationRepository.save(reservation);

        return ResponseEntity.ok(mapToResponse(updated));
    }

    // UPDATE: Update reservation details (ADMIN or owner if status is PENDING)
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().isEqual(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + authentication.getName()));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));

        if (user.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to update this reservation");
        }

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));

        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(request.getPrice());

        Reservation updated = reservationRepository.save(reservation);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // DELETE: ADMIN can delete reservation
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFoundException("Reservation not found with id: " + id);
        }
        reservationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ReservationResponse mapToResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getUser().getEmail(),
                r.getResource().getName(),
                r.getStartTime(),
                r.getEndTime(),
                r.getPrice(),
                r.getStatus().name()
        );
    }
}