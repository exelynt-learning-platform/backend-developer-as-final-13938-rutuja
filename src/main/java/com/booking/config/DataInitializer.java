package com.booking.config;

import com.booking.model.Role;
import com.booking.model.User;
import com.booking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		if (userRepository.findByEmail("admin@booking.com").isEmpty()) {
			userRepository.save(User.builder().email("admin@booking.com").password(passwordEncoder.encode("Admin@123"))
					.role(Role.ADMIN).build());
		}
		if (userRepository.findByEmail("user@booking.com").isEmpty()) {
			userRepository.save(User.builder().email("user@booking.com").password(passwordEncoder.encode("User@123"))
					.role(Role.USER).build());
		}
	}
}