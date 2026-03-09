package com.smartcampus;

import com.smartcampus.dto.request.ResourceRequest;
import com.smartcampus.dto.response.ResourceResponse;
import com.smartcampus.entity.Resource;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.service.impl.ResourceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock private ResourceRepository resourceRepository;
    @InjectMocks private ResourceServiceImpl resourceService;

    @Test
    void createResource_Success() {
        ResourceRequest req = new ResourceRequest();
        req.setName("Lab 101");
        req.setType(ResourceType.LAB);
        req.setLocation("Block A");
        req.setCapacity(30);

        Resource saved = Resource.builder()
                .id(1L).name("Lab 101").type(ResourceType.LAB)
                .location("Block A").capacity(30).status(ResourceStatus.ACTIVE).build();

        when(resourceRepository.save(any())).thenReturn(saved);

        ResourceResponse result = resourceService.createResource(req);
        assertNotNull(result);
        assertEquals("Lab 101", result.getName());
        assertEquals(ResourceStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getResourceById_NotFound_ThrowsException() {
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> resourceService.getResourceById(99L));
    }

    @Test
    void getAllResources_ReturnsAll() {
        Resource r1 = Resource.builder().id(1L).name("Room A").type(ResourceType.MEETING_ROOM)
                .location("Floor 1").status(ResourceStatus.ACTIVE).build();
        Resource r2 = Resource.builder().id(2L).name("Lab B").type(ResourceType.LAB)
                .location("Floor 2").status(ResourceStatus.ACTIVE).build();

        when(resourceRepository.findAll()).thenReturn(List.of(r1, r2));

        List<ResourceResponse> results = resourceService.getAllResources();
        assertEquals(2, results.size());
    }
}
