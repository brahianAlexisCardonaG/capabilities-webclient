package com.capabilities.project.infraestructure.entrypoints.handler;

import com.capabilities.project.domain.api.BootcampCapabilityServicePort;
import com.capabilities.project.domain.api.CapabilityServicePort;
import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.infraestructure.entrypoints.dto.BootcampCapabilityDto;
import com.capabilities.project.infraestructure.entrypoints.dto.BootcampIdsDto;
import com.capabilities.project.infraestructure.entrypoints.dto.CapabilityDto;
import com.capabilities.project.infraestructure.entrypoints.mapper.CapabilityMapper;
import com.capabilities.project.infraestructure.entrypoints.util.ApiResponse;
import com.capabilities.project.infraestructure.entrypoints.util.ApiResponseMap;
import com.capabilities.project.infraestructure.entrypoints.util.error.ApplyErrorHandler;
import com.capabilities.project.infraestructure.entrypoints.util.validate.BootcampCapabilityValidation;
import com.capabilities.project.infraestructure.entrypoints.util.validate.ValidateRequestSave;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologyCapabilityResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

import static com.capabilities.project.infraestructure.entrypoints.util.Constants.CAPABILITY_ERROR;
import static com.capabilities.project.infraestructure.entrypoints.util.Constants.X_MESSAGE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CapabilityHandlerImpl {

    private final CapabilityServicePort capabilityServicePort;
    private final CapabilityMapper capabilityMapper;
    private final ValidateRequestSave validateRequestSave;
    private final ApplyErrorHandler applyErrorHandler;
    private final BootcampCapabilityValidation bootcampCapabilityValidation;
    private final BootcampCapabilityServicePort bootcampCapabilityServicePort;
    String messageId = "6165416541";

    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<ServerResponse> response = validateRequestSave.validateAndMapRequest(request)
                .map(capabilityMapper::toCapability)
                .transform(capabilityServicePort::save)
                .collectList()
                .flatMap(list -> {
                    var dtos = list.stream()
                            .map(capabilityMapper::toCapabilityDto)
                            .collect(Collectors.toList());
                    return ServerResponse.status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.builder()
                                    .code(TechnicalMessage.CAPABILITY_CREATED.getMessage())
                                    .message(TechnicalMessage.CAPABILITY_CREATED.getMessage())
                                    .date(Instant.now().toString())
                                    .data(dtos)
                                    .build());
                })
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnError(ex -> log.error(CAPABILITY_ERROR, ex));

        return applyErrorHandler.applyErrorHandling(response, messageId);
    }

    public Mono<ServerResponse> getTechnologiesByCapabilitiesIds(ServerRequest request) {
        List<Long> ids = request.queryParams().getOrDefault("capabilityIds", List.of()).stream()
                .flatMap(p -> Arrays.stream(p.split("\\s*,\\s*")))
                .map(Long::parseLong)
                .toList();
        String order = request.queryParam("order").orElse("asc");
        int rows = Integer.parseInt(request.queryParam("rows").orElse("4"));
        int skip = Integer.parseInt(request.queryParam("skip").orElse("0"));

        Mono<ServerResponse> response = capabilityServicePort.findTechnologiesByIdCapabilities(ids, order, skip, rows)
                .flatMap(map -> {
                    var data = map.entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> e.getValue().stream()
                                            .map(t -> new TechnologyResponse(t.getId(), t.getName()))
                                            .collect(Collectors.toList()),
                                    (a, b) -> a,
                                    LinkedHashMap::new
                            ));
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(TechnologyCapabilityResponse.builder()
                                    .code(TechnicalMessage.CAPABILITY_TECHNOLOGIES_FOUND.getCode())
                                    .message(TechnicalMessage.CAPABILITY_TECHNOLOGIES_FOUND.getMessage())
                                    .date(Instant.now().toString())
                                    .data(data)
                                    .build());
                })
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnError(ex -> log.error("Error getting technologies: {}", ex.getMessage(), ex));

        return applyErrorHandler.applyErrorHandling(response, messageId);
    }

    public Mono<ServerResponse> createCapabilityRelateTechnologies(ServerRequest request) {

        Mono<ServerResponse> response = validateRequestSave.validateAndMapRequest(request)
                .map(capabilityMapper::toCapability)
                .transform(capabilityServicePort::saveCapabilityTechnology)
                .flatMap(ct -> {
                    var data = ct.getData().entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> e.getValue().stream()
                                            .map(t -> new TechnologyResponse(t.getId(), t.getName()))
                                            .collect(Collectors.toList()),
                                    (a, b) -> a,
                                    LinkedHashMap::new
                            ));
                    return ServerResponse.status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(TechnologyCapabilityResponse.builder()
                                    .code(TechnicalMessage.CAPABILITY_TECHNOLOGY_RELATION.getCode())
                                    .message(TechnicalMessage.CAPABILITY_TECHNOLOGY_RELATION.getMessage())
                                    .date(ct.getDate())
                                    .data(data)
                                    .build());
                })
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnError(ex -> log.error(CAPABILITY_ERROR, ex)).next();

        return applyErrorHandler.applyErrorHandling(response, messageId);
    }

    public Mono<ServerResponse> createBootcampCapability(ServerRequest request) {
        Mono<ServerResponse> response = request.bodyToMono(BootcampCapabilityDto.class)
                .flatMap(bootcampCapabilityValidation::validateDuplicateIds)
                .flatMap(dto -> {
                    if (dto.getBootcampId() == null || dto.getCapabilityIds() == null || dto.getCapabilityIds().isEmpty()) {
                        return Mono.error(new BusinessException(TechnicalMessage.INVALID_PARAMETERS));
                    }
                    return bootcampCapabilityServicePort.saveBootcampCapabilities(dto.getBootcampId(), dto.getCapabilityIds());
                })
                .then(ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.builder()
                                .code(TechnicalMessage.BOOTCAMP_CAPABILITY_CREATED.getMessage())
                                .message(TechnicalMessage.BOOTCAMP_CAPABILITY_CREATED.getMessage())
                                .date(Instant.now().toString())
                                .build()
                        )
                ).contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnError(ex -> log.error(CAPABILITY_ERROR, ex));

        return applyErrorHandler.applyErrorHandling(response, messageId);
    }

    public Mono<ServerResponse> getCapabilitiesListByBootcampIds(ServerRequest request) {
        Mono<ServerResponse> response = request.bodyToMono(BootcampIdsDto.class)
                .flatMapMany(dto -> Flux.fromIterable(dto.getBootcampIds()))
                .flatMap(bootcampId ->
                        bootcampCapabilityServicePort.findCapabilitiesByBootcamp(bootcampId)
                                .map(capList -> {
                                    List<CapabilityDto> dtoList = capList.stream()
                                            .map(capabilityMapper::toCapabilityDto)
                                            .toList();
                                    return Map.entry(bootcampId, dtoList);
                                })
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue) // Map<Long, List<CapabilityDto>>
                .flatMap(resultMap ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponseMap.builder()
                                        .code(TechnicalMessage.BOOTCAMP_CAPABILITY_FOUND.getCode())
                                        .message(TechnicalMessage.BOOTCAMP_CAPABILITY_FOUND.getMessage())
                                        .date(Instant.now().toString())
                                        .data(resultMap) // este data ahora es un Map<Long, List<TechnologyDto>>
                                        .build())
                )
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnError(ex -> log.error(CAPABILITY_ERROR, ex));

        return applyErrorHandler.applyErrorHandling(response, messageId);

    }

}
