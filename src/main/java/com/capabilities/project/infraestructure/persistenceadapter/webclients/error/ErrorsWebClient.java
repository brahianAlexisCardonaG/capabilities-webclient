package com.capabilities.project.infraestructure.persistenceadapter.webclients.error;

import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.ProcessorException;
import com.capabilities.project.infraestructure.entrypoints.util.error.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

public class ErrorsWebClient {
    private static final String BASE_URL = "http://localhost:8081";

    public static Mono<Throwable> handleError(Mono<String> errorBodyMono) {
        return errorBodyMono.flatMap(errorBody -> {
            try {
                ErrorDto errorDto = new ObjectMapper().readValue(errorBody, ErrorDto.class);
                TechnicalMessage technicalMessage = TechnicalMessage.INVALID_REQUEST;
                return Mono.error(new ProcessorException(errorDto.getMessage(), technicalMessage));
            } catch (Exception e) {
                return Mono.error(new ProcessorException("Error parsing error response: " +
                        BASE_URL  + " " + errorBody,
                        TechnicalMessage.INTERNAL_ERROR));
            }
        });
    }
}
