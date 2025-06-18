package com.capabilities.project.infraestructure.entrypoints.util.validation;

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
                .flatMapMany(dtoList ->
                        capabilityValidationDto.validateNoDuplicateNames(dtoList)
                                .thenMany(Flux.fromIterable(dtoList))
                )
                .flatMap(dto ->
                        capabilityValidationDto.validateLengthWords(dto)
                                .then(capabilityValidationDto.validateFieldNotNullOrBlank(dto))
                                .thenReturn(dto) // Si pasó todas, devolvemos el DTO
                );
    }
}
