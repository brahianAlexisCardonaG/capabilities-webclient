package com.capabilities.project.domain.model.webclient.technology.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiTechnologyMessage {
    private String code;
    private String message;
    private String date;
}
