package com.capabilities.project.domain.api;

import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.domain.model.client.technology.CapabilityTechnology;
import com.capabilities.project.domain.model.client.technology.Technology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface CapabilityServicePort {
    Flux<Capability> save(Flux<Capability> capability);

    Mono<Map<String, List<Technology>>> findTechnologiesByIdCapabilities(List<Long> capabilityId,
                                                                         String order,
                                                                         int skip,
                                                                         int rows);

    Mono<CapabilityTechnology> saveCapabilityTechnology(Flux<Capability> capability);
}
