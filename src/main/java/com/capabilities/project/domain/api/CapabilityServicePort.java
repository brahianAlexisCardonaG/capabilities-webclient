package com.capabilities.project.domain.api;

import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.domain.model.client.technology.CapabilityListTechnology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapabilityServicePort {

    Mono<List<CapabilityListTechnology>> saveCapabilityTechnology(Flux<Capability> capabilityFlux);


    Mono<List<CapabilityListTechnology>> findTechnologiesByIdCapabilitiesModel(
            List<Long> capabilityIds,
            String order,
            int skip,
            int rows);

    Flux<Capability> getCapabilityByIds(List<Long> capabilityIds);
}
