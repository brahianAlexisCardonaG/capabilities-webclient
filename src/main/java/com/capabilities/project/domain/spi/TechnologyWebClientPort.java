package com.capabilities.project.domain.spi;

import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologiesMessageResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologyCapabilityResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyWebClientPort {
    Mono<TechnologyCapabilityResponse> getTechnologiesByCapabilityIds(List<Long> capabilityIds);

    Mono<TechnologyCapabilityResponse> saveRelateTechnologiesCapabilities(Long capabilityId,
                                                                          List<Long> technologyIds);

    Mono<TechnologiesMessageResponse> getTechnologiesByIds(List<Long> technologyIds);
}
