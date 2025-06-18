package com.capabilities.project.infraestructure.entrypoints.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CapabilityDto {
    private String name;
    private String description;
    private List<Long> technologyIds;
}
