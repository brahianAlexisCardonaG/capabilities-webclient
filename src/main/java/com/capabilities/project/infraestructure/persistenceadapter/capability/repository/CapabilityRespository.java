package com.capabilities.project.infraestructure.persistenceadapter.capability.repository;

import com.capabilities.project.infraestructure.persistenceadapter.capability.entity.CapabilityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CapabilityRespository extends ReactiveCrudRepository<CapabilityEntity, Long> {
    Mono<CapabilityEntity> findByName(String name);
}
