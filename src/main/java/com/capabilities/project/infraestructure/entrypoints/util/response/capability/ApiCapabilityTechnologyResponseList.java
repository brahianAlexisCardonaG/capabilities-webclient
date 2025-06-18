package com.capabilities.project.infraestructure.entrypoints.util.response.capability;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApiCapabilityTechnologyResponseList {
    private String code;
    private String message;
    private String date;
    List<CapabilityListTechnologyResponse> data;
}
