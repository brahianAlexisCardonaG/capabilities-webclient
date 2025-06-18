package com.capabilities.project.domain.api;

import com.capabilities.project.domain.model.capability.Capability;
import com.capabilities.project.domain.model.capability.CapabilityListTechnology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapabilityServicePort {

    Mono<List<CapabilityListTechnology>> saveCapabilityTechnology(List<Capability> capabilityList);


    Mono<List<CapabilityListTechnology>> findTechnologiesByIdCapabilitiesModel(
            List<Long> capabilityIds,
            String order,
            int skip,
            int rows);

    Mono<List<Capability>> getCapabilityByIds(List<Long> capabilityIds);
}
