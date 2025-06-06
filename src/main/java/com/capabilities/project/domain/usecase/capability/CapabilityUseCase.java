package com.capabilities.project.domain.usecase.capability;

import com.capabilities.project.domain.api.CapabilityServicePort;
import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.domain.model.client.technology.CapabilityListTechnology;
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
    public Mono<List<CapabilityListTechnology>> saveCapabilityTechnology(Flux<Capability> capabilityFlux) {
        return transactionalOperator.transactional(
                capabilityFlux
                        .flatMap(capability ->
                                // 1) Validar que la capability no exista por nombre
                                capabilityPersistencePort.findByName(capability.getName())
                                        .flatMap(exists -> {
                                            if (exists) {
                                                return Mono.error(new BusinessException(TechnicalMessage.CAPABILITY_ALREADY_EXISTS));
                                            }
                                            return Mono.just(capability);
                                        })
                                        // 2) Validar que las tecnologías existan (mediante el WebClient)
                                        .flatMap(validCapability ->
                                                technologyWebClientPort.getTechnologiesByIds(validCapability.getTechnologyIds())
                                                        .flatMap(response -> {
                                                            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                                                                return Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_NOT_EXISTS));
                                                            }
                                                            return Mono.just(validCapability);
                                                        })
                                        )
                                        // 3) Persistir la capability y guardar las relaciones de tecnologías
                                        .flatMap(validCapability ->
                                                capabilityPersistencePort
                                                        .save(Flux.just(validCapability))
                                                        .next()
                                                        .flatMap(savedCapability ->
                                                                technologyWebClientPort
                                                                        .saveRelateTechnologiesCapabilities(
                                                                                savedCapability.getId(),
                                                                                validCapability.getTechnologyIds()
                                                                        )
                                                                        .thenReturn(savedCapability)
                                                        )
                                        )
                        )
                        // Reunimos todas las capabilities ya guardadas
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
                                            return Mono.error(new RuntimeException("Technology response o data es null"));
                                        }

                                        // 6) Construir la lista de CapabilityListTechnologies
                                        List<CapabilityListTechnology> listaSinOrdenar = response.getData().entrySet().stream()
                                                .map(entry -> {
                                                    Long capId = Long.valueOf(entry.getKey());
                                                    String capName = idToName.get(capId);
                                                    if (capName == null) {
                                                        throw new RuntimeException("Capability no existe en BD: " + capId);
                                                    }

                                                    // Convertir cada DTO a domain model Technology
                                                    List<Technology> techsDomain = entry.getValue().stream()
                                                            .map(dto -> technologyMapper.toDomain(dto, capName))
                                                            .toList();

                                                    return new CapabilityListTechnology(capId, capName, techsDomain);
                                                })
                                                .toList();

                                        // 7) Ordenar según el parámetro 'order' (por defecto asc)
                                        List<CapabilityListTechnology> listaOrdenada =
                                                CapabilityOrder.sortList(listaSinOrdenar, "asc");

                                        // 8) Paginación: [skip, skip+rows)
                                        List<CapabilityListTechnology> listaPaginada = CapabilityPaginator.paginateList(
                                                listaOrdenada,
                                                0,
                                                listaOrdenada.size()    // rows: tamaño completo
                                        );

                                        return Mono.just(listaPaginada);
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
                                    return Mono.error(new RuntimeException("Technology response o data es null"));
                                }

                                // Construyo la lista inicial sin orden ni paginación
                                List<CapabilityListTechnology> listaSinOrdenar = response.getData().entrySet().stream()
                                        .map(entry -> {
                                            Long capId = Long.valueOf(entry.getKey());
                                            String capName = capabilityNamesMap.get(capId);
                                            if (capName == null) {
                                                throw new RuntimeException("Capability no existe en BD: " + capId);
                                            }

                                            // Cada elemento entry.getValue() es List<TechnologyDTO> del WebClient
                                            // El mapper de WebClient ya convierte cada DTO a domain model Technology.
                                            List<Technology> techsDomain = entry.getValue().stream()
                                                    .map(dto -> technologyMapper.toDomain(dto, capName))
                                                    .toList();

                                            return new CapabilityListTechnology(capId, capName, techsDomain);
                                        })
                                        .toList();

                                // 1) Ordenar según el parámetro `order`
                                List<CapabilityListTechnology> listaOrdenada =
                                        CapabilityOrder.sortList(listaSinOrdenar, order);

                                // 2) Paginación: extraer sublista de [skip, skip+rows)
                                List<CapabilityListTechnology> listaPaginada = CapabilityPaginator.paginateList(
                                        listaOrdenada,
                                        skip,
                                        rows
                                );

                                return Mono.just(listaPaginada);
                            });
                });
    }

    @Override
    public Flux<Capability> getCapabilityByIds(List<Long> capabilityIds) {
        return capabilityPersistencePort.findByIds(capabilityIds)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.CAPABILITIES_NOT_EXISTS)));
    }
}