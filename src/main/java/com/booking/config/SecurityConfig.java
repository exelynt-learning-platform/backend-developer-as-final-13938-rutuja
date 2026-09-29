package com.booking.config;

import com.booking.security.AuthTokenFilter;
import com.booking.serviceImpl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final UserDetailsServiceImpl userDetailsService;
	private final AuthTokenFilter authTokenFilter;

	public SecurityConfig(UserDetailsServiceImpl userDetailsService, AuthTokenFilter authTokenFilter) {
		this.userDetailsService = userDetailsService;
		this.authTokenFilter = authTokenFilter;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/auth/**").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers(HttpMethod.POST, "/resources/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/resources/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/resources/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/resources/**").hasAnyRole("ADMIN", "USER")
						.requestMatchers(HttpMethod.PUT, "/reservations/{id}/status").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/reservations/**").hasRole("ADMIN")
						.requestMatchers("/reservations/**").hasAnyRole("ADMIN", "USER")
						.anyRequest().authenticated());

		http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}