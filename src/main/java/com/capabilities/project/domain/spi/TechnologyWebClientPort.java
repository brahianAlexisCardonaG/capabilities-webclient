package com.capabilities.project.domain.spi;

import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologiesMessageResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.ApiCapabilityTechnologyResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyWebClientPort {
    Mono<ApiCapabilityTechnologyResponse> getTechnologiesByCapabilityIds(List<Long> capabilityIds);

    Mono<ApiCapabilityTechnologyResponse> saveRelateTechnologiesCapabilities(Long capabilityId,
                                                                             List<Long> technologyIds);

    Mono<TechnologiesMessageResponse> getTechnologiesByIds(List<Long> technologyIds);
}
