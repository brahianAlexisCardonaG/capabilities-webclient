package com.capabilities.project.infraestructure.persistenceadapter.webclients.mapper;

import com.capabilities.project.domain.model.client.technology.CapabilityListTechnology;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.CapabilityListTechnologyResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CapabilityTechnologyMapperResponse {
    CapabilityListTechnologyResponse toCapabilityListTechnologiesResponse
            (CapabilityListTechnology capabilityListTechnology);
}
