package com.capabilities.project.domain.spi;

import com.capabilities.project.domain.model.capability.Capability;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BootcampCapabilityPersistencePort {

    Mono<List<Long>> findCapabilitiesByBootcamp(Long bootcampId);

    Mono<Void> saveRelations(Long bootcampId, List<Long> capabilityIds);

    Mono<List<Capability>> findCapabilitiesListByBootcamp(Long bootcampId);

    Mono<Boolean> existsBootcampById(Long bootcampId);

    Flux<Long> findBootcampsByCapabilitiesIds(List<Long> capabilityIds);

    Mono<Void> deleteBootcampsCapabilities(List<Long> capabilityIds);

}