package com.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponse {
	private Long id;
	private String userEmail;
	private String resourceName;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private BigDecimal price;
	private String status;
}