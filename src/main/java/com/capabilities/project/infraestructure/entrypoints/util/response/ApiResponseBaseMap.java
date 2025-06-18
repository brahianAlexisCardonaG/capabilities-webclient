package com.capabilities.project.infraestructure.entrypoints.util.response;

import com.capabilities.project.infraestructure.entrypoints.util.error.ErrorDto;
import com.capabilities.project.infraestructure.entrypoints.util.response.capability.CapabilityResponse;
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
public class ApiResponseBaseMap {
    private String code;
    private String message;
    private String date;
    private Map<Long, List<CapabilityResponse>> data;
    private List<ErrorDto> errors;
}