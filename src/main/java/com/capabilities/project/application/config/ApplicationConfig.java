package com.capabilities.project.application.config;

import com.capabilities.project.domain.api.BootcampCapabilityServicePort;
import com.capabilities.project.domain.api.CapabilityServicePort;
import com.capabilities.project.domain.spi.BootcampCapabilityPersistencePort;
import com.capabilities.project.domain.spi.CapabilityPersistencePort;
import com.capabilities.project.domain.spi.TechnologyWebClientPort;
import com.capabilities.project.domain.usecase.bootcampcapability.BootcampCapabilityUseCase;
import com.capabilities.project.domain.usecase.bootcampcapability.util.ValidationBootcampCapability;
import com.capabilities.project.domain.usecase.capability.CapabilityUseCase;
import com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.BootcampCapabilityPersistenceAdapter;
import com.capabilities.project.infraestructure.persistenceadapter.capability.CapabilityPersistenceAdapter;
import com.capabilities.project.infraestructure.persistenceadapter.capability.mapper.CapabilityEntityMapper;
import com.capabilities.project.infraestructure.persistenceadapter.capability.repository.CapabilityRespository;
import com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.repository.BootcampCapabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final CapabilityRespository capabilityRespository;
    private final CapabilityEntityMapper capabilityEntityMapper;
    private final BootcampCapabilityRepository bootcampCapabilityRepository;


    @Bean
    public CapabilityPersistencePort capabilityPersistencePort() {
        return new CapabilityPersistenceAdapter(capabilityRespository,
                capabilityEntityMapper);
    }

    @Bean
    public CapabilityServicePort capabilityServicePort(CapabilityPersistencePort capabilityPersistencePort,
                                                       TechnologyWebClientPort technologyWebClientPort,
                                                       TransactionalOperator transactionalOperator) {
        return new CapabilityUseCase(capabilityPersistencePort, technologyWebClientPort, transactionalOperator);
    }

    @Bean
    public BootcampCapabilityPersistencePort bootcampCapabilityPersistencePort() {
        return new BootcampCapabilityPersistenceAdapter(bootcampCapabilityRepository,
                capabilityRespository,
                capabilityEntityMapper);
    }

    @Bean
    public BootcampCapabilityServicePort bootcampCapabilityServicePort(CapabilityPersistencePort capabilityPersistencePort,
                                                                       BootcampCapabilityPersistencePort bootcampCapabilityPersistencePort,
                                                                       ValidationBootcampCapability validationBootcampCapability
                                                               ) {
        return new BootcampCapabilityUseCase(capabilityPersistencePort,
                bootcampCapabilityPersistencePort,
                validationBootcampCapability);
    }


}
