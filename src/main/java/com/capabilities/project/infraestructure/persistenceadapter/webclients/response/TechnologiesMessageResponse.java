package com.capabilities.project.infraestructure.persistenceadapter.webclients.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TechnologiesMessageResponse {
    private String code;
    private String message;
    private String date;
    List<TechnologyResponse> data;
}
