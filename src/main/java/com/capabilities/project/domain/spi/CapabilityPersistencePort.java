package com.capabilities.project.domain.spi;

import com.capabilities.project.domain.model.capability.Capability;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapabilityPersistencePort {
    Mono<Capability> save(Capability capability);
    Mono<Boolean> findByName(String name);
    Mono<List<Capability>> findByAllIds(List<Long> ids);
    Mono<Boolean> existsById(Long capabilityId);
    Mono<List<Capability>> findByIds(List<Long> capabilityIds);
    Mono<Void> deleteCapabilities(List<Long> capabilityIds);
}