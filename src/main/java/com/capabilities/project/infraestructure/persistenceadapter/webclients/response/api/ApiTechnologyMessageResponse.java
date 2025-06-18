package com.capabilities.project.infraestructure.persistenceadapter.webclients.response.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiTechnologyMessageResponse {
    private String code;
    private String message;
    private String date;
}
