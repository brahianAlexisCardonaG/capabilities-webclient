package com.capabilities.project.infraestructure.entrypoints.util.response;

import com.capabilities.project.infraestructure.entrypoints.dto.CapabilityDto;
import com.capabilities.project.infraestructure.entrypoints.util.error.ErrorDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ApiResponseBase {
    private String code;
    private String message;
    private String date;
    private List<CapabilityDto> data;
    private List<ErrorDto> errors;
}
