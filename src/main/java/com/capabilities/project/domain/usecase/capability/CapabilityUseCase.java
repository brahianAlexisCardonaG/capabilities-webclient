package com.capabilities.project.domain.usecase.capability;

import com.capabilities.project.domain.api.CapabilityServicePort;
import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.domain.model.capability.Capability;
import com.capabilities.project.domain.model.capability.CapabilityListTechnology;
import com.capabilities.project.domain.model.webclient.technology.Technology;
import com.capabilities.project.domain.spi.CapabilityPersistencePort;
import com.capabilities.project.domain.spi.TechnologyWebClientPort;
import com.capabilities.project.domain.usecase.capability.util.CapabilityOrder;
import com.capabilities.project.domain.usecase.capability.util.CapabilityPaginator;
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
    private final TransactionalOperator transactionalOperator;


    @Override
    public Mono<List<CapabilityListTechnology>> saveCapabilityTechnology(List<Capability> capabilityList) {
        return transactionalOperator.transactional(
                Flux.fromIterable(capabilityList)
                        .flatMap(capability ->
                                // 1) Validar que la capability no exista por nombre
                                capabilityPersistencePort.findByName(capability.getName())
                                        .flatMap(exists -> {
                                            if (exists) {
                                                return Mono.error(new BusinessException(TechnicalMessage.CAPABILITY_ALREADY_EXISTS));
                                            }
                                            return Mono.just(capability);
                                        })
                        )
                        // 2) Validar que las tecnologías existan (mediante el WebClient)
                        .flatMap(validCapability ->
                                technologyWebClientPort.getTechnologiesByIds(validCapability.getTechnologyIds())
                                        .flatMap(response -> {
                                            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                                                return Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_NOT_EXISTS));
                                            }
                                            return Mono.just(validCapability);
                                        })
                        )// 3) Persistir la capability y guardar las relaciones de tecnologías
                        .flatMap(validCapability ->
                                capabilityPersistencePort.save(validCapability)
                                        .flatMap(savedCapability ->
                                                technologyWebClientPort
                                                        .saveRelateTechnologiesCapabilities(
                                                                savedCapability.getId(),
                                                                validCapability.getTechnologyIds()
                                                        )
                                                        .thenReturn(savedCapability)
                                        )
                        )
                        .collectList()
                        .flatMap(savedCapabilities -> {
                            // 4) Extraer los IDs y mapear ID → nombre
                            List<Long> savedIds = savedCapabilities.stream()
                                    .map(Capability::getId)
                                    .toList();
                            Map<Long, String> idToName = savedCapabilities.stream()
                                    .collect(Collectors.toMap(
                                            Capability::getId,
                                            Capability::getName
                                    ));

                            // 5) Llamamos a getTechnologiesByCapabilityIds para obtener las tecnologías recién asociadas
                            return technologyWebClientPort.getTechnologiesByCapabilityIds(savedIds)
                                    .flatMap(response -> {
                                        if (response == null || response.getData() == null) {
                                            return Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_NOT_EXISTS));
                                        }

                                        Map<String, List<Technology>> dataMap = response.getData();

                                        // 6) Construir la lista de CapabilityListTechnology
                                        List<CapabilityListTechnology> resultList = savedCapabilities.stream()
                                                .map(savedCap -> {
                                                    String key = String.valueOf(savedCap.getId());
                                                    List<Technology> techsForCap = dataMap.getOrDefault(key, List.of());
                                                    return CapabilityListTechnology.builder()
                                                            .id(savedCap.getId())
                                                            .name(savedCap.getName())
                                                            .technologies(techsForCap)
                                                            .build();
                                                })
                                                .toList();

                                        return Mono.just(resultList);
                                    });
                        })
        );
    }


    @Override
    public Mono<List<CapabilityListTechnology>> findTechnologiesByIdCapabilitiesModel(
            List<Long> capabilityIds,
            String order,
            int skip,
            int rows) {

        return capabilityPersistencePort.findByAllIds(capabilityIds)
                .flatMap(capabilities -> {
                    if (capabilities.size() != capabilityIds.size()) {
                        return Mono.error(new BusinessException(TechnicalMessage.CAPABILITY_NOT_EXISTS));
                    }

                    // Mapeo ID -> nombre de cada capability
                    Map<Long, String> capabilityNamesMap = capabilities.stream()
                            .collect(Collectors.toMap(Capability::getId, Capability::getName));

                    return technologyWebClientPort.getTechnologiesByCapabilityIds(capabilityIds)
                            .flatMap(response -> {
                                if (response == null || response.getData() == null) {
                                    return Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_NOT_EXISTS));
                                }

                                // Validación defensiva separada
                                boolean missingCapabilities = response.getData().keySet().stream()
                                        .map(Long::valueOf)
                                        .anyMatch(id -> !capabilityNamesMap.containsKey(id));
                                if (missingCapabilities) {
                                    return Mono.error(new BusinessException(TechnicalMessage.CAPABILITIES_NOT_EXISTS));
                                }

                                // Construyo la lista inicial sin orden ni paginación
                                List<CapabilityListTechnology> listWithoutOrder = response.getData().entrySet().stream()
                                        .map(entry -> {
                                            Long capId = Long.valueOf(entry.getKey());
                                            String capName = capabilityNamesMap.get(capId);

                                            List<Technology> techsDomain = entry.getValue().stream()
                                                    .map(dto -> Technology.builder()
                                                            .id(dto.getId())
                                                            .name(dto.getName())
                                                            .build()
                                                    )
                                                    .toList();

                                            return CapabilityListTechnology.builder()
                                                    .id(capId)
                                                    .name(capName)
                                                    .technologies(techsDomain)
                                                    .build();
                                        })
                                        .toList();

                                // 1) Ordenar según el parámetro `order`
                                List<CapabilityListTechnology> listOrder =
                                        CapabilityOrder.sortList(listWithoutOrder, order);

                                // 2) Paginación: extraer sublista de [skip, skip+rows)
                                List<CapabilityListTechnology> listPaginate = CapabilityPaginator.paginateList(
                                        listOrder,
                                        skip,
                                        rows
                                );

                                return Mono.just(listPaginate);
                            });
                });
    }

    @Override
    public Mono<List<Capability>> getCapabilityByIds(List<Long> capabilityIds) {
        return capabilityPersistencePort.findByIds(capabilityIds)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.CAPABILITIES_NOT_EXISTS)));
    }
}