package com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability;

import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.domain.spi.BootcampCapabilityPersistencePort;
import com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.entity.BootcampCapabilityEntity;
import com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.repository.BootcampCapabilityRepository;
import com.capabilities.project.infraestructure.persistenceadapter.capability.mapper.CapabilityEntityMapper;
import com.capabilities.project.infraestructure.persistenceadapter.capability.repository.CapabilityRespository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class BootcampCapabilityPersistenceAdapter implements BootcampCapabilityPersistencePort {

    private final BootcampCapabilityRepository bootcampCapabilityRepository;
    private final CapabilityRespository capabilityRespository;
    private final CapabilityEntityMapper capabilityEntityMapper;

    @Override
    public Mono<List<Long>> findCapabilitiesByBootcamp(Long bootcampId) {
        return bootcampCapabilityRepository.findByIdBootcamp(bootcampId)
                .map(BootcampCapabilityEntity::getIdCapability)
                .collectList();
    }

    @Override
    public Mono<Void> saveRelations(Long bootcampId, List<Long> capabilityIds) {
        return Flux.fromIterable(capabilityIds)
                .map(capId -> new BootcampCapabilityEntity(null, bootcampId, capId))
                .collectList()
                .flatMapMany(bootcampCapabilityRepository::saveAll)
                .then();
    }

    @Override
    public Mono<List<Capability>> findCapabilitiesListByBootcamp(Long bootcampId) {
        return bootcampCapabilityRepository.findByIdBootcamp(bootcampId)
                .map(BootcampCapabilityEntity::getIdCapability)
                .collectList()
                .flatMap(capIds ->
                        capabilityRespository.findAllById(capIds)
                                .map(capabilityEntityMapper::toModel)
                                .collectList()
                );
    }

    @Override
    public Mono<Boolean> existsBootcampById(Long bootcampId) {
        return bootcampCapabilityRepository.findByIdBootcamp(bootcampId)
                .hasElements();
    }

    @Override
    public Flux<Long> findBootcampsByCapabilitiesIds(List<Long> capabilityIds) {
        return bootcampCapabilityRepository.findIdsBootcampsByCapabilityIds(capabilityIds);
    }

    @Override
    public Mono<Void> deleteBootcampsCapabilities(List<Long> capabilityIds) {
        return bootcampCapabilityRepository.findAll()
                .filter(entity -> capabilityIds.contains(entity.getIdCapability()))
                .collectList()
                .flatMapMany(bootcampCapabilityRepository::deleteAll)
                .then();
    }
}
