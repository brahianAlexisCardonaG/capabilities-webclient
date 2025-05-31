package com.capabilities.project.infraestructure.persistenceadapter.webclients.mapper;

import com.capabilities.project.domain.model.client.technology.Technology;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TechnologyMapper {
    @Mapping(target = "capabilityName", source = "capabilityName")
    @Mapping(target = "name", source = "response.name")
    Technology toDomain(TechnologyResponse response, String capabilityName);

}
