package com.smartcampus.service.impl;

import com.smartcampus.dto.request.ResourceRequest;
import com.smartcampus.dto.response.ResourceResponse;
import com.smartcampus.entity.Resource;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    @Override
    public ResourceResponse createResource(ResourceRequest req) {
        Resource resource = Resource.builder()
                .name(req.getName())
                .type(req.getType())
                .location(req.getLocation())
                .capacity(req.getCapacity())
                .description(req.getDescription())
                .availableFrom(req.getAvailableFrom())
                .availableTo(req.getAvailableTo())
                .status(req.getStatus() != null ? req.getStatus() : ResourceStatus.ACTIVE)
                .build();
        return toResponse(resourceRepository.save(resource));
    }

    @Override
    public ResourceResponse updateResource(Long id, ResourceRequest req) {
        Resource resource = findById(id);
        resource.setName(req.getName());
        resource.setType(req.getType());
        resource.setLocation(req.getLocation());
        resource.setCapacity(req.getCapacity());
        resource.setDescription(req.getDescription());
        resource.setAvailableFrom(req.getAvailableFrom());
        resource.setAvailableTo(req.getAvailableTo());
        if (req.getStatus() != null) resource.setStatus(req.getStatus());
        return toResponse(resourceRepository.save(resource));
    }

    @Override
    public void deleteResource(Long id) {
        if (!resourceRepository.existsById(id)) throw new ResourceNotFoundException("Resource not found: " + id);
        resourceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources() {
        return resourceRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> searchResources(ResourceType type, ResourceStatus status, String location, Integer minCapacity) {
        return resourceRepository.searchResources(type, status, location, minCapacity)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Resource findById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
    }

    public ResourceResponse toResponse(Resource r) {
        return ResourceResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .type(r.getType())
                .location(r.getLocation())
                .capacity(r.getCapacity())
                .description(r.getDescription())
                .availableFrom(r.getAvailableFrom())
                .availableTo(r.getAvailableTo())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
