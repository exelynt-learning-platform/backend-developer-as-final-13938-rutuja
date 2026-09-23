package com.booking.controller;

import com.booking.dto.ReservationRequest;
import com.booking.dto.ReservationResponse;
import com.booking.model.*;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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

	public ReservationController(ReservationRepository reservationRepository, ResourceRepository resourceRepository,
			UserRepository userRepository) {
		this.reservationRepository = reservationRepository;
		this.resourceRepository = resourceRepository;
		this.userRepository = userRepository;
	}

	@PostMapping

	public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request,
			Authentication authentication) {
		User user = userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		Resource resource = resourceRepository.findById(request.getResourceId())
				.orElseThrow(() -> new RuntimeException("Resource not found"));

		Reservation reservation = Reservation.builder().user(user).resource(resource).startTime(request.getStartTime())
				.endTime(request.getEndTime()).price(request.getPrice()).status(Status.PENDING).build();

		Reservation saved = reservationRepository.save(reservation);
		return ResponseEntity.ok(mapToResponse(saved));
	}

	@GetMapping
	public ResponseEntity<Page<ReservationResponse>> getReservations(@RequestParam(required = false) Status status,
			@RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id") String sortBy, Authentication authentication) {

		User user = userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		Long userId = user.getRole() == Role.ADMIN ? null : user.getId();

		Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
		Page<Reservation> reservations = reservationRepository.findWithFilters(status, minPrice, maxPrice, userId,
				pageable);

		return ResponseEntity.ok(reservations.map(this::mapToResponse));
	}

	// ADMIN: Update reservation status
	@PutMapping("/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ReservationResponse> updateReservationStatus(@PathVariable Long id,
			@RequestParam Status status) {

		Reservation reservation = reservationRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

		reservation.setStatus(status);
		Reservation updated = reservationRepository.save(reservation);

		return ResponseEntity.ok(mapToResponse(updated));
	}

	private ReservationResponse mapToResponse(Reservation r) {
		return new ReservationResponse(r.getId(), r.getUser().getEmail(), r.getResource().getName(), r.getStartTime(),
				r.getEndTime(), r.getPrice(), r.getStatus().name());
	}
}