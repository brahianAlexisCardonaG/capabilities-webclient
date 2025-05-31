package com.capabilities.project.infraestructure.persistenceadapter.capability;

import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.domain.spi.CapabilityPersistencePort;
import com.capabilities.project.infraestructure.persistenceadapter.capability.mapper.CapabilityEntityMapper;
import com.capabilities.project.infraestructure.persistenceadapter.capability.repository.CapabilityRespository;
import com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.repository.BootcampCapabilityRepository;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@AllArgsConstructor
public class CapabilityPersistenceAdapter implements CapabilityPersistencePort {
    private final CapabilityRespository capabilityRespository;
    private final CapabilityEntityMapper capabilityEntityMapper;

    @Override
    public Mono<Boolean> findByName(String name) {
        return capabilityRespository.findByName(name)
                .map(capabilityEntityMapper::toModel)
                .map(tech -> true)  // Si encuentra el usuario, devuelve true
                .defaultIfEmpty(false);  // Si no encuentra, devuelve false
    }


    @Override
    public Mono<List<Capability>> findByAllIds(List<Long> ids) {
        return Flux.fromIterable(ids)
                .collectList() // opcional, si ids ya viene como lista
                .flatMapMany(capabilityRespository::findAllById)// devuelve Flux<Capability>
                .map(capabilityEntityMapper::toModel)
                .collectList(); // convierte Flux<Capability> en Mono<List<Capability>>
    }

    @Override
    public Mono<Boolean> existsById(Long capabilityId) {
        return capabilityRespository.findById(capabilityId)
                .map(capabilityEntityMapper::toModel)
                .map(tech -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Flux<Capability> save(Flux<Capability> capability) {
        return capability
                .map(capabilityEntityMapper::toEntity)
                .collectList()
                .flatMapMany(capabilityRespository::saveAll)
                .map(capabilityEntityMapper::toModel);
    }
}
