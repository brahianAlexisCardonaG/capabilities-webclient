package com.capabilities.project.infraestructure.persistenceadapter.webclients.response;

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
