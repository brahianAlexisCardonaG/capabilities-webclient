package com.capabilities.project.infraestructure.entrypoints.mapper;

import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.infraestructure.entrypoints.dto.CapabilityDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CapabilityMapper {

    Capability toCapability(CapabilityDto capabilityDto);

    CapabilityDto toCapabilityDto(Capability capability);
}
