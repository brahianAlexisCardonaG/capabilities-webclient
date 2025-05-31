package com.capabilities.project.infraestructure.entrypoints.util.error;

import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.infraestructure.entrypoints.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static com.capabilities.project.infraestructure.entrypoints.util.Constants.X_MESSAGE_ID;

@Component
public class BuildErrorResponse {
    public Mono<ServerResponse> buildErrorResponse(HttpStatus httpStatus, String identifier, TechnicalMessage error,
                                                    List<ErrorDto> errors) {
        return Mono.defer(() -> {
            ApiResponse apiErrorResponse = ApiResponse
                    .builder()
                    .code(error.getCode())
                    .message(error.getMessage())
                    .identifier(identifier)
                    .date(Instant.now().toString())
                    .errors(errors)
                    .build();
            return ServerResponse.status(httpStatus)
                    .bodyValue(apiErrorResponse);
        });
    }


    public String getMessageId(ServerRequest serverRequest) {
        return serverRequest.headers().firstHeader(X_MESSAGE_ID);
    }
}
