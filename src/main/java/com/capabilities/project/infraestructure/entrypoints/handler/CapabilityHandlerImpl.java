package com.capabilities.project.infraestructure.entrypoints.handler;

import com.capabilities.project.domain.api.BootcampCapabilityServicePort;
import com.capabilities.project.domain.api.CapabilityServicePort;
import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.infraestructure.entrypoints.dto.BootcampCapabilityDto;
import com.capabilities.project.infraestructure.entrypoints.dto.CapabilityDto;
import com.capabilities.project.infraestructure.entrypoints.mapper.CapabilityMapper;
import com.capabilities.project.infraestructure.entrypoints.mapper.CapabilityMapperResponse;
import com.capabilities.project.infraestructure.entrypoints.util.response.ApiResponseBase;
import com.capabilities.project.infraestructure.entrypoints.util.response.ApiResponseBaseMap;
import com.capabilities.project.infraestructure.entrypoints.util.error.ApplyErrorHandler;
import com.capabilities.project.infraestructure.entrypoints.util.response.capability.CapabilityResponse;
import com.capabilities.project.infraestructure.entrypoints.util.validation.BootcampCapabilityValidation;
import com.capabilities.project.infraestructure.entrypoints.util.validation.ValidateRequestSave;
import com.capabilities.project.infraestructure.entrypoints.util.response.capability.ApiCapabilityTechnologyResponseList;
import com.capabilities.project.infraestructure.entrypoints.util.response.capability.CapabilityListTechnologyResponse;
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
    private final CapabilityMapperResponse capabilityMapperResponse;

    public Mono<ServerResponse> getTechnologiesByCapabilitiesIds(ServerRequest request) {
        List<Long> ids = request.queryParams().getOrDefault("capabilityIds", List.of()).stream()
                .flatMap(p -> Arrays.stream(p.split("\\s*,\\s*")))
                .map(Long::parseLong)
                .toList();
        String order = request.queryParam("order").orElse("asc");
        int rows = Integer.parseInt(request.queryParam("rows").orElse("4"));
        int skip = Integer.parseInt(request.queryParam("skip").orElse("0"));

        Mono<ServerResponse> response = capabilityServicePort.findTechnologiesByIdCapabilitiesModel(ids, order, skip, rows)
                .flatMap(listData -> {

                    List<CapabilityListTechnologyResponse> responseList = listData.stream()
                            .map(capabilityMapperResponse::toCapabilityListTechnologiesResponse)
                            .toList();

                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiCapabilityTechnologyResponseList.builder()
                                    .code(TechnicalMessage.CAPABILITY_TECHNOLOGIES_FOUND.getCode())
                                    .message(TechnicalMessage.CAPABILITY_TECHNOLOGIES_FOUND.getMessage())
                                    .date(Instant.now().toString())
                                    .data(responseList)
                                    .build());
                })
                .contextWrite(Context.of(X_MESSAGE_ID, ""))
                .doOnError(ex -> log.error(CAPABILITY_ERROR, ex));

        return applyErrorHandler.applyErrorHandling(response);
    }

    public Mono<ServerResponse> createCapabilityRelateTechnologies(ServerRequest request) {

        Mono<ServerResponse> response = validateRequestSave.validateAndMapRequest(request)
                .map(capabilityMapper::toCapability)
                .collectList()
                .flatMap(capabilityServicePort::saveCapabilityTechnology)
                .map(list -> list.stream()
                        .map(capabilityMapperResponse::toCapabilityListTechnologiesResponse)
                        .toList())
                .flatMap(ct -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiCapabilityTechnologyResponseList.builder()
                                .code(TechnicalMessage.CAPABILITY_TECHNOLOGY_RELATION.getCode())
                                .message(TechnicalMessage.CAPABILITY_TECHNOLOGY_RELATION.getMessage())
                                .date(Instant.now().toString())
                                .data(ct)
                                .build()))
                .contextWrite(Context.of(X_MESSAGE_ID, ""))
                .doOnError(ex -> log.error(CAPABILITY_ERROR, ex));

        return applyErrorHandler.applyErrorHandling(response);
    }

    public Mono<ServerResponse> createBootcampCapability(ServerRequest request) {
        Mono<ServerResponse> response = request.bodyToMono(BootcampCapabilityDto.class)
                .flatMap(bootcampCapabilityValidation::validateDuplicateIds)
                .flatMap(bootcampCapabilityValidation::validateFieldNotNullOrBlank)
                .flatMap(dto -> bootcampCapabilityServicePort.saveBootcampCapabilities(dto.getBootcampId(), dto.getCapabilityIds()))
                .then(ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponseBase.builder()
                                .code(TechnicalMessage.BOOTCAMP_CAPABILITY_CREATED.getMessage())
                                .message(TechnicalMessage.BOOTCAMP_CAPABILITY_CREATED.getMessage())
                                .date(Instant.now().toString())
                                .build()
                        )
                ).contextWrite(Context.of(X_MESSAGE_ID, ""))
                .doOnError(ex -> log.error(CAPABILITY_ERROR, ex));

        return applyErrorHandler.applyErrorHandling(response);
    }

    public Mono<ServerResponse> getCapabilitiesListByBootcampIds(ServerRequest request) {

        List<Long> bootcampIds = request.queryParams().getOrDefault("bootcampIds", List.of()).stream()
                .flatMap(p -> Arrays.stream(p.split("\\s*,\\s*")))
                .map(Long::parseLong)
                .toList();

        Mono<ServerResponse> response = Flux.fromIterable(bootcampIds)
                .flatMap(bootcampId ->
                        bootcampCapabilityServicePort.findCapabilitiesByBootcamp(bootcampId)
                                .map(capList -> {
                                    List<CapabilityResponse> dtoList = capList.stream()
                                            .map(capabilityMapperResponse::toCapabilityResponse)
                                            .toList();
                                    return Map.entry(bootcampId, dtoList);
                                })
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue) // Map<Long, List<CapabilityDto>>
                .flatMap(resultMap ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponseBaseMap.builder()
                                        .code(TechnicalMessage.BOOTCAMP_CAPABILITY_FOUND.getCode())
                                        .message(TechnicalMessage.BOOTCAMP_CAPABILITY_FOUND.getMessage())
                                        .date(Instant.now().toString())
                                        .data(resultMap)
                                        .build())
                )
                .contextWrite(Context.of(X_MESSAGE_ID, ""))
                .doOnError(ex -> log.error(CAPABILITY_ERROR, ex));

        return applyErrorHandler.applyErrorHandling(response);

    }

    public Mono<ServerResponse> getCapabilitiesByIds(ServerRequest request) {
        List<Long> technologyIds = request.queryParams()
                .get("ids")
                .stream()
                .flatMap(param -> Arrays.stream(param.split(","))) // Manejo de lista en query param
                .map(Long::parseLong)
                .toList();

        Mono<ServerResponse> response = capabilityServicePort.getCapabilityByIds(technologyIds)
                .flatMapMany(Flux::fromIterable)
                .map(capabilityMapperResponse::toCapabilityResponse)
                .collectList()
                .flatMap(techList -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponseBase.builder()
                                .code(TechnicalMessage.CAPABILITIES_FOUND.getCode())
                                .message(TechnicalMessage.CAPABILITIES_FOUND.getMessage())
                                .date(Instant.now().toString())
                                .data(techList)
                                .build()
                        )
                )
                .contextWrite(Context.of(X_MESSAGE_ID, ""))
                .doOnError(ex -> log.error(CAPABILITY_ERROR, ex));

        return applyErrorHandler.applyErrorHandling(response);
    }

    public Mono<ServerResponse> deleteBootcampsCapabilities(ServerRequest request) {
        List<Long> capabilityIds = request.queryParams()
                .get("capabilityIds")
                .stream()
                .flatMap(param -> Arrays.stream(param.split(",")))
                .map(Long::parseLong)
                .toList();

        Mono<ServerResponse> response =bootcampCapabilityServicePort.deleteBootcampsCapabilities(capabilityIds)
                .then(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponseBase.builder()
                                .code(TechnicalMessage.BOOTCAMPS_CAPABILITIES_DELETE.getCode())
                                .message(TechnicalMessage.BOOTCAMPS_CAPABILITIES_DELETE.getMessage())
                                .date(Instant.now().toString())
                                .build()
                        )
                )
                .contextWrite(Context.of(X_MESSAGE_ID, ""))
                .doOnError(ex -> log.error(CAPABILITY_ERROR, ex));

        return applyErrorHandler.applyErrorHandling(response);
    }

}
