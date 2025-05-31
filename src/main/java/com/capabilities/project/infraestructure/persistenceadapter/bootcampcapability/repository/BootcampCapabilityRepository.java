package com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.repository;

import com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.entity.BootcampCapabilityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BootcampCapabilityRepository extends ReactiveCrudRepository<BootcampCapabilityEntity, Long> {

    Flux<BootcampCapabilityEntity> findByIdBootcamp(Long idBootcamp);

}
