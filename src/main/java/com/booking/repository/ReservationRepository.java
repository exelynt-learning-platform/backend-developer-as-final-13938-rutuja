package com.booking.repository;

import com.booking.model.Reservation;
import com.booking.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	@Query("SELECT r FROM Reservation r WHERE (:status IS NULL OR r.status = :status) "
			+ "AND (:minPrice IS NULL OR r.price >= :minPrice) " + "AND (:maxPrice IS NULL OR r.price <= :maxPrice) "
			+ "AND (:userId IS NULL OR r.user.id = :userId)")
	Page<Reservation> findWithFilters(@Param("status") Status status, @Param("minPrice") BigDecimal minPrice,
			@Param("maxPrice") BigDecimal maxPrice, @Param("userId") Long userId, Pageable pageable);
}