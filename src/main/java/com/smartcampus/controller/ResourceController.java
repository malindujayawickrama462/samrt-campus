package com.smartcampus.controller;

import com.smartcampus.dto.request.ResourceRequest;
import com.smartcampus.dto.response.ResourceResponse;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    // GET /api/resources - PUBLIC (search & filter)
    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getResources(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) ResourceStatus status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minCapacity) {
        return ResponseEntity.ok(resourceService.searchResources(type, status, location, minCapacity));
    }

    // GET /api/resources/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResource(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    // POST /api/resources - ADMIN only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceResponse> createResource(@Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.createResource(request));
    }

    // PUT /api/resources/{id} - ADMIN only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceResponse> updateResource(@PathVariable Long id,
                                                           @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(resourceService.updateResource(id, request));
    }

    // DELETE /api/resources/{id} - ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
