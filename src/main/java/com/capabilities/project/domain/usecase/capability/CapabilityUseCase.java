package com.capabilities.project.domain.usecase.capability;

import com.capabilities.project.domain.api.CapabilityServicePort;
import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.domain.model.client.technology.CapabilityTechnology;
import com.capabilities.project.domain.model.client.technology.Technology;
import com.capabilities.project.domain.spi.CapabilityPersistencePort;
import com.capabilities.project.domain.spi.TechnologyWebClientPort;
import com.capabilities.project.domain.usecase.capability.util.CapabilityOrder;
import com.capabilities.project.domain.usecase.capability.util.CapabilityPaginator;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.mapper.TechnologyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CapabilityUseCase implements CapabilityServicePort {

    private final CapabilityPersistencePort capabilityPersistencePort;
    private final TechnologyWebClientPort technologyWebClientPort;
    private final TechnologyMapper technologyMapper;
    private final TransactionalOperator transactionalOperator;


    @Override
    public Flux<Capability> save(Flux<Capability> capability) {

        return capability
                .distinct(Capability::getName)
                .flatMap(cap ->
                        capabilityPersistencePort.findByName(cap.getName())
                                .filter(exists -> !exists)
                                .switchIfEmpty(Mono
                                        .error(new BusinessException(TechnicalMessage
                                                .CAPABILITY_ALREADY_EXISTS)))
                                .flatMap(ignored ->
                                        capabilityPersistencePort.save(Flux.just(cap))
                                                .next()
                                )
                                .flux()
                );
    }

    @Override
    public Mono<Map<String, List<Technology>>> findTechnologiesByIdCapabilities(List<Long> capabilityIds,
                                                                                String order,
                                                                                int skip,
                                                                                int rows) {
        return capabilityPersistencePort.findByAllIds(capabilityIds)
                .flatMap(capabilities -> {
                    if (capabilities.size() != capabilityIds.size()) {
                        return Mono.error(new BusinessException(TechnicalMessage.CAPABILITY_NOT_EXISTS));
                    }

                    // Mapear id -> nombre para luego inyectar el nombre en cada tecnología
                    Map<Long, String> capabilityNamesMap = capabilities.stream()
                            .collect(Collectors.toMap(Capability::getId, Capability::getName));

                    return technologyWebClientPort.getTechnologiesByCapabilityIds(capabilityIds)
                            .doOnNext(response -> System.out.println("Response data: " + response))
                            .flatMapMany(response -> {
                                if (response == null || response.getData() == null) {
                                    return Flux.error(new RuntimeException("Technology response or data is null"));
                                }
                                return Flux.fromIterable(response.getData().entrySet());
                            })
                            .flatMap(entry -> {
                                Long capabilityId = Long.valueOf(entry.getKey());
                                String capName = capabilityNamesMap.get(capabilityId);

                                if (capName == null) {
                                    return Mono.error(new BusinessException(TechnicalMessage.CAPABILITIES_NOT_EXISTS));
                                }

                                List<Technology> technologies = entry.getValue().stream()
                                        .map(resp -> technologyMapper.toDomain(resp, capName))
                                        .toList();

                                return Mono.just(Map.entry(capName, technologies));
                            })
                            .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                            .map(unsortedMap -> {
                                // Primero ordenamos el mapa (usando tu CapabilitySorter)
                                Map<String, List<Technology>> sortedMap = CapabilityOrder.sort(unsortedMap, order);
                                // Luego paginamos el mapa ordenado
                                return CapabilityPaginator.paginate(sortedMap, skip, rows);
                            });

                });
    }

    @Override
    public Mono<CapabilityTechnology> saveCapabilityTechnology(Flux<Capability> capabilityFlux) {
        return transactionalOperator.transactional(capabilityFlux.flatMap(capability ->
                        // 1. Validar que la capability no exista por nombre
                        capabilityPersistencePort.findByName(capability.getName())
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new BusinessException(TechnicalMessage.CAPABILITY_ALREADY_EXISTS));
                                    }
                                    return Mono.just(capability);
                                })
                                // 2. Validar que las tecnologías existan
                                .flatMap(validCapability ->
                                        technologyWebClientPort.getTechnologiesByIds(validCapability.getTechnologyIds())
                                                .flatMap(response -> {
                                                    if (response == null ||
                                                            response.getData() == null ||
                                                            response.getData().isEmpty()) {
                                                        return Mono
                                                                .error(new BusinessException(TechnicalMessage
                                                                        .TECHNOLOGY_NOT_EXISTS));
                                                    }
                                                    return Mono.just(validCapability);
                                                })
                                )
                                // 3. Persistir la capability y guardar las relaciones usando TechnologyClient
                                .flatMap(validCapability ->
                                        capabilityPersistencePort.save(Flux.just(validCapability))
                                                .next()
                                                .flatMap(savedCapability -> {
                                                    // Invocamos el endpoint del TechnologyClient para guardar las relaciones
                                                    return technologyWebClientPort.saveRelateTechnologiesCapabilities(
                                                                    savedCapability.getId(),
                                                                    validCapability.getTechnologyIds()
                                                            )
                                                            .thenReturn(savedCapability);
                                                })
                                )
                )
                .collectList() // Se recogen todas las capabilities guardadas
                .flatMap(savedCapabilities -> {
                    // Extraer los IDs y construir un mapa de id a capability's name
                    List<Long> savedIds = savedCapabilities.stream()
                            .map(Capability::getId)
                            .collect(Collectors.toList());

                    Map<Long, String> idToName = savedCapabilities.stream()
                            .collect(Collectors.toMap(Capability::getId, Capability::getName));

                    // 4. Obtener la data enriquecida desde el TechnologyClient
                    return technologyWebClientPort.getTechnologiesByCapabilityIds(savedIds)
                            .map(response -> {
                                // Convertir el mapa de la respuesta: la key actual es el id (en String)
                                // y se transforma a capacidad usando el mapa idToName.
                                Map<String, List<Technology>> transformedData = response
                                        .getData()
                                        .entrySet()
                                        .stream()
                                        .collect(Collectors.toMap(
                                                entry -> {
                                                    Long capId = Long.valueOf(entry.getKey());
                                                    String capName = idToName.get(capId);
                                                    return capName != null ? capName : entry.getKey();
                                                },
                                                entry -> entry.getValue().stream()
                                                        // Se aplica el mapper para transformar cada tecnología asignando el nombre de la capability
                                                        .map(
                                                                resp ->
                                                                        technologyMapper.toDomain(resp, idToName.get(Long.valueOf(entry.getKey()))))
                                                        .collect(Collectors.toList()),
                                                (e1, e2) -> e1,
                                                LinkedHashMap::new
                                        ));

                                // 5. Crear el objeto de dominio CapabilityTechnology con la data transformada.
                                return new CapabilityTechnology(
                                        response.getCode(),
                                        response.getMessage(),
                                        response.getIdentifier(),
                                        response.getDate(),
                                        transformedData
                                );
                            });
                })
        );
    }

}