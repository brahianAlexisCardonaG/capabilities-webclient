package com.capabilities.project.infraestructure.entrypoints.util;

import com.capabilities.project.infraestructure.entrypoints.dto.CapabilityDto;
import com.capabilities.project.infraestructure.entrypoints.util.error.ErrorDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseMap {
    private String code;
    private String message;
    private String identifier;
    private String date;
    private Map<Long, List<CapabilityDto>> data;
    private List<ErrorDto> errors;
}