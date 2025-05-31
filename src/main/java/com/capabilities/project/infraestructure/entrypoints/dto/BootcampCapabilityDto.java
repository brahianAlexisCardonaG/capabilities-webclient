package com.capabilities.project.infraestructure.entrypoints.dto;

import lombok.Data;

import java.util.List;

@Data
public class BootcampCapabilityDto {
    private Long bootcampId;
    private List<Long> capabilityIds;
}
