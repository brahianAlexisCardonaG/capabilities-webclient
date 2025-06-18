package com.capabilities.project.domain.spi;

import com.capabilities.project.domain.model.webclient.technology.api.ApiListTechnology;
import com.capabilities.project.domain.model.webclient.technology.api.ApiMapTechnology;
import com.capabilities.project.domain.model.webclient.technology.api.ApiTechnologyMessage;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyWebClientPort {
    Mono<ApiMapTechnology> getTechnologiesByCapabilityIds(List<Long> capabilityIds);

    Mono<ApiTechnologyMessage> saveRelateTechnologiesCapabilities(Long capabilityId,
                                                                  List<Long> technologyIds);

    Mono<ApiListTechnology> getTechnologiesByIds(List<Long> technologyIds);
}
