package com.capabilities.project.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TechnicalMessage {
    INTERNAL_ERROR("500", "Something went wrong, please try again", ""),
    INTERNAL_ERROR_IN_ADAPTERS("PRC501", "Something went wrong in adapters, please try again", ""),
    INVALID_REQUEST("400", "Bad Request, please verify data", ""),
    INVALID_PARAMETERS(INVALID_REQUEST.getCode(), "Bad Parameters, please verify data", ""),
    INVALID_MESSAGE_ID("404", "Invalid Message ID, please verify", "messageId"),
    UNSUPPORTED_OPERATION("501", "Method not supported, please try again", ""),
    CAPABILITY_CREATED("201", "Capabilities created successfully", ""),
    ADAPTER_RESPONSE_NOT_FOUND("404-0", "invalid Capabilities, please verify", ""),
    CAPABILITY_ALREADY_EXISTS("400", "The Capability already found register.", ""),
    NAME_TOO_LONG("404-1", "The name must not exceed the 50 characters", ""),
    DESCRIPTION_TOO_LONG("404-2", "The Description must not exceed the 90 characters", ""),
    CAPABILITY_TECHNOLOGY_RELATION("201", "Capacidades creadas con exito y Tecnológicas asociadas con exito", ""),
    CAPABILITY_NOT_EXISTS("400", "The capabilityId not found.", ""),
    CAPABILITY_ONE_ASSOCIATION("404-3", "A bootcamp must be associated with at least 1 technology", ""),
    CAPABILITY_FOUR_ASSOCIATION("404-4", "A bootcamp cannot have more than 4 associated technologies", ""),
    CAPABILITY_DUPLICATED_TECHNOLOGIES("404-5", "Technologies cannot be duplicated", ""),
    CAPABILITY_TECH_ALREADY_ASSOCIATED("404-6", "The capability or capabilities are already associated with this bootcamp", ""),
    CAPABILITIES_NOT_EXISTS("400", "Some of the Capabilities are not registered", ""),
    CAPABILITY_TECHNOLOGIES_FOUND("200", "Capability with tecnologies found", ""),
    DUPLICATE_NAMES_CAPABILITY("404-7", "The names of the Capabilities cannot be the same", ""),
    TECHNOLOGY_NOT_EXISTS("400", " Some of the technologies are not registered.", ""),
    CAPABILITIES_DUPLICATES_IDS("400-8","Check the input data, it is trying to save the same capabilities",""),
    BOOTCAMP_CAPABILITY_CREATED("201-1", "Relations created successfully", ""),
    BOOTCAMP_CAPABILITY_FOUND("200","Capabilities by Bootcamps found","");

    private final String code;
    private final String message;
    private final String param;

}
