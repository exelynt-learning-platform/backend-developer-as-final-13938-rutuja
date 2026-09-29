package com.booking.dto;

public class AuthResponse {

	private String token;
	private String role;

	// Default constructor (Required for JSON deserialization)
	public AuthResponse() {
	}

	// Parameterized constructor
	public AuthResponse(String token, String role) {
		this.token = token;
		this.role = role;
	}

	// Getters and Setters (Required for Jackson JSON serialization)
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}