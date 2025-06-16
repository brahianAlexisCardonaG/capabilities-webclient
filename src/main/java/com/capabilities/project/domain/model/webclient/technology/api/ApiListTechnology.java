package com.capabilities.project.domain.model.webclient.technology.api;

import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologyResponse;

import java.util.List;

public class ApiListTechnology{
    private String code;
    private String message;
    private String date;
    private List<TechnologyResponse> data;
}
