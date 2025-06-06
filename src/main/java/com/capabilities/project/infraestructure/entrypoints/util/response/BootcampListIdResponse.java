package com.capabilities.project.infraestructure.entrypoints.util.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BootcampListIdResponse {
    private String code;
    private String message;
    private String date;
    private List<Long> data;
}
