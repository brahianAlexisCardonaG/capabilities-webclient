package com.capabilities.project.domain.api;

import com.capabilities.project.domain.model.Capability;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BootcampCapabilityServicePort {
    Mono<Void> saveBootcampCapabilities(Long bootcampId, List<Long> capabilityIds);
    Mono<List<Capability>> findCapabilitiesByBootcamp(Long bootcampId);
}
