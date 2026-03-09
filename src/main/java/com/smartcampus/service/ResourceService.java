package com.smartcampus.service;

import com.smartcampus.dto.request.ResourceRequest;
import com.smartcampus.dto.response.ResourceResponse;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;

import java.util.List;

public interface ResourceService {
    ResourceResponse createResource(ResourceRequest request);
    ResourceResponse updateResource(Long id, ResourceRequest request);
    void deleteResource(Long id);
    ResourceResponse getResourceById(Long id);
    List<ResourceResponse> getAllResources();
    List<ResourceResponse> searchResources(ResourceType type, ResourceStatus status, String location, Integer minCapacity);
}
