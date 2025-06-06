package com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.repository;

import com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.entity.BootcampCapabilityEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface BootcampCapabilityRepository extends ReactiveCrudRepository<BootcampCapabilityEntity, Long> {

    Flux<BootcampCapabilityEntity> findByIdBootcamp(Long idBootcamp);

    @Query("SELECT id_bootcamp FROM capability_bootcamp WHERE id_capability IN (:idCapabilities)")
    Flux<Long> findIdsBootcampsByCapabilityIds(List<Long> idCapabilities);

}
