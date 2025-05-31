package com.capabilities.project.infraestructure.entrypoints.util.error;

import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.domain.exception.ProcessorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplyErrorHandler {

    private final BuildErrorResponse buildErrorRes;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<ServerResponse> applyErrorHandling(Mono<ServerResponse> mono, String msgId) {
        return mono
                .onErrorResume(ProcessorException.class, ex -> {
                    log.error("ProcessorException: {}", ex.getMessage());
                    HttpStatus status = TechnicalMessage.INTERNAL_ERROR.equals(ex.getTechnicalMessage())
                            ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
                    return buildErrorRes.buildErrorResponse(
                            status, msgId, ex.getTechnicalMessage(),
                            List.of(ErrorDto.builder().code(ex.getTechnicalMessage().getCode()).message(ex.getMessage()).build())
                    );
                })
                .onErrorResume(BusinessException.class, ex -> buildErrorRes.buildErrorResponse(
                        HttpStatus.BAD_REQUEST, msgId, TechnicalMessage.INVALID_PARAMETERS,
                        List.of(ErrorDto.builder().code(ex.getTechnicalMessage().getCode()).message(ex.getTechnicalMessage().getMessage()).param(ex.getTechnicalMessage().getParam()).build())
                ))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("WebClientResponseException: {}", ex.getResponseBodyAsString());
                    try {
                        ErrorDto err = objectMapper.readValue(ex.getResponseBodyAsString(), ErrorDto.class);
                        return buildErrorRes.buildErrorResponse(
                                HttpStatus.valueOf(ex.getRawStatusCode()), msgId, TechnicalMessage.INVALID_REQUEST,
                                List.of(ErrorDto.builder().code(err.getCode()).message(err.getMessage()).param(err.getParam()).build())
                        );
                    } catch (Exception parseEx) {
                        log.error("Error parsing error response: {}", parseEx.getMessage());
                        return buildErrorRes.buildErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR, msgId, TechnicalMessage.INTERNAL_ERROR,
                                List.of(ErrorDto.builder().code(TechnicalMessage.INTERNAL_ERROR.getCode()).message(TechnicalMessage.INTERNAL_ERROR.getMessage()).build())
                        );
                    }
                })
                .onErrorResume(ex -> buildErrorRes.buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR, msgId, TechnicalMessage.INTERNAL_ERROR,
                        List.of(ErrorDto.builder().code(TechnicalMessage.INTERNAL_ERROR.getCode()).message(TechnicalMessage.INTERNAL_ERROR.getMessage()).build())
                ));
    }
}
