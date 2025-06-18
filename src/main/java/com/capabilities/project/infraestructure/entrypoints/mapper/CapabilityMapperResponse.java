package com.capabilities.project.infraestructure.entrypoints.mapper;

import com.capabilities.project.domain.model.capability.Capability;
import com.capabilities.project.domain.model.capability.CapabilityListTechnology;
import com.capabilities.project.infraestructure.entrypoints.util.response.capability.CapabilityListTechnologyResponse;
import com.capabilities.project.infraestructure.entrypoints.util.response.capability.CapabilityResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CapabilityMapperResponse {
    CapabilityListTechnologyResponse toCapabilityListTechnologiesResponse
            (CapabilityListTechnology capabilityListTechnology);

    CapabilityResponse toCapabilityResponse(Capability capability);
}
