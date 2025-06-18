package com.capabilities.project.domain.model.webclient.technology.api;

import com.capabilities.project.domain.model.webclient.technology.Technology;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiListTechnology{
    private String code;
    private String message;
    private String date;
    private List<Technology> data;
}
