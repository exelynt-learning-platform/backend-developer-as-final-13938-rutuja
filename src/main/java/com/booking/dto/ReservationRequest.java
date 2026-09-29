package com.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

	@NotNull(message = "Resource ID is required")
	private Long resourceId;

	@NotNull(message = "Start time is required")
	@Future(message = "Start time must be in the future")
	private LocalDateTime startTime;

	@NotNull(message = "End time is required")
	@Future(message = "End time must be in the future")
	private LocalDateTime endTime;

	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
	private BigDecimal price;
}