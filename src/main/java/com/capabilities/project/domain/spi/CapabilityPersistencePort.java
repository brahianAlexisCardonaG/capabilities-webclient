package com.capabilities.project.domain.spi;

import com.capabilities.project.domain.model.Capability;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapabilityPersistencePort {
    Flux<Capability> save(Flux<Capability> capability);
    Mono<Boolean> findByName(String name);
    Mono<List<Capability>> findByAllIds(List<Long> ids);
    Mono<Boolean> existsById(Long capabilityId);
}