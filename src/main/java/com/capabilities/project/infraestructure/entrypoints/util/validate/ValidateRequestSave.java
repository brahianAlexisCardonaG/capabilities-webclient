package com.capabilities.project.infraestructure.entrypoints.util.validate;

import com.capabilities.project.infraestructure.entrypoints.dto.CapabilityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class ValidateRequestSave {
    private final CapabilityValidationDto capabilityValidationDto;

    public Flux<CapabilityDto> validateAndMapRequest(ServerRequest request) {
        return request.bodyToFlux(CapabilityDto.class)
                .collectList()
                .flatMap(capabilityValidationDto::validateNoDuplicateNames)
                .flatMapMany(Flux::fromIterable)
                .flatMap(capabilityValidationDto::validateLengthWords);
    }
}
