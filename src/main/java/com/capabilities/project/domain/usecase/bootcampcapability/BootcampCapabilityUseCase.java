package com.capabilities.project.domain.usecase.bootcampcapability;

import com.capabilities.project.domain.api.BootcampCapabilityServicePort;
import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.domain.spi.BootcampCapabilityPersistencePort;
import com.capabilities.project.domain.spi.CapabilityPersistencePort;
import com.capabilities.project.domain.usecase.bootcampcapability.util.ValidationBootcampCapability;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BootcampCapabilityUseCase implements BootcampCapabilityServicePort {

    private final CapabilityPersistencePort capabilityPersistencePort;
    private final BootcampCapabilityPersistencePort bootcampCapabilityPersistencePort;
    private final ValidationBootcampCapability validationBootcampCapability;

    @Override
    public Mono<Void> saveBootcampCapabilities(Long bootcampId, List<Long> capabilityIds) {
        return Flux.fromIterable(capabilityIds)
                .flatMap(id -> capabilityPersistencePort.existsById(id)
                        .flatMap(exists -> {
                            if (!exists) {
                                return Mono.error(new BusinessException(TechnicalMessage.CAPABILITIES_NOT_EXISTS));
                            }
                            return Mono.just(id);
                        })
                )
                .collectList()
                .flatMap(validCapabilityIds ->
                        // Paso 2: Obtener tecnologías ya asociadas a la capacidad
                        bootcampCapabilityPersistencePort.findCapabilitiesByBootcamp(bootcampId)
                                .flatMap(existingCapabilities -> {
                                    Set<Long> existingSet = new HashSet<>(existingCapabilities);

                                    // Paso 3: Validar duplicados y número total
                                    return validationBootcampCapability
                                            .validateHasDuplicatesCapabilities(existingSet, validCapabilityIds)
                                            .then(validationBootcampCapability.validateNumberCapabilities(existingCapabilities, validCapabilityIds))
                                            .then(bootcampCapabilityPersistencePort.
                                                    saveRelations(bootcampId, validCapabilityIds));
                                })
                );
    }

    @Override
    public Mono<List<Capability>> findCapabilitiesByBootcamp(Long bootcampId) {
        return bootcampCapabilityPersistencePort.existsBootcampById(bootcampId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BusinessException(TechnicalMessage.CAPABILITIES_NOT_EXISTS));
                    }
                    return bootcampCapabilityPersistencePort.findCapabilitiesListByBootcamp(bootcampId);
                });
    }


    @Override
    public Mono<Void> deleteBootcampsCapabilities(List<Long> capabilityIds) {
        return bootcampCapabilityPersistencePort.findBootcampsByCapabilitiesIds(capabilityIds)
                .collect(Collectors.toSet())
                .flatMap(bootcampIds -> {
                    if (bootcampIds.size() > 1) {
                        return Mono.error(new BusinessException(TechnicalMessage.BOOTCAMPS_CAPABILITIES_MORE_ONE_RELATE));
                    }
                    return bootcampCapabilityPersistencePort.deleteBootcampsCapabilities(capabilityIds)
                            .then(capabilityPersistencePort.deleteCapabilities(capabilityIds));
                });
    }
}
