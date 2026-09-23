package com.booking.controller;

import com.booking.model.Resource;
import com.booking.repository.ResourceRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resources")
public class ResourceController {

	private final ResourceRepository resourceRepository;

	public ResourceController(ResourceRepository resourceRepository) {
		this.resourceRepository = resourceRepository;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Resource> createResource(@Valid @RequestBody Resource resource) {
		Resource savedResource = resourceRepository.save(resource);
		return ResponseEntity.ok(savedResource);
	}

	// ADMIN & USER: View all resources
	@GetMapping
	public ResponseEntity<List<Resource>> getAllResources() {
		List<Resource> resources = resourceRepository.findAll();
		return ResponseEntity.ok(resources);
	}

	// ADMIN: Delete a resource
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
		resourceRepository.deleteById(id);
		return ResponseEntity.ok().build();
	}
}