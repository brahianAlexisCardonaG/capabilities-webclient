package com.capabilities.project.infraestructure.persistenceadapter.capability.mapper;

import com.capabilities.project.domain.model.capability.Capability;
import com.capabilities.project.infraestructure.persistenceadapter.capability.entity.CapabilityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CapabilityEntityMapper {
    @Mapping(target = "technologyIds", ignore = true)
    Capability toModel(CapabilityEntity capabilityEntity);
    CapabilityEntity toEntity(Capability capability);
}
