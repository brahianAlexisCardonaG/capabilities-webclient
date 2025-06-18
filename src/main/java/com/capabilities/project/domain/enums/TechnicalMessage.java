package com.capabilities.project.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TechnicalMessage {
    INTERNAL_ERROR("500", "Something went wrong, please try again", ""),
    INVALID_REQUEST("400", "Bad Request, please verify data", ""),
    INVALID_PARAMETERS(INVALID_REQUEST.getCode(), "Bad Parameters, please verify data", ""),
    CAPABILITY_ALREADY_EXISTS("400", "The Capability already found register.", ""),
    NAME_TOO_LONG("404-1", "The name must not exceed the 50 characters", ""),
    DESCRIPTION_TOO_LONG("404-2", "The Description must not exceed the 90 characters", ""),
    CAPABILITY_TECHNOLOGY_RELATION("201", "Successfully Created Capabilities and Successfully Associated Technologies", ""),
    CAPABILITY_NOT_EXISTS("400", "The capabilityId not found.", ""),
    CAPABILITY_ONE_ASSOCIATION("404-3", "A bootcamp must be associated with at least 1 capability", ""),
    CAPABILITY_FOUR_ASSOCIATION("404-4", "A bootcamp cannot have more than 4 associated capability", ""),
    CAPABILITY_TECH_ALREADY_ASSOCIATED("404-6", "The capability or capabilities are already associated with this bootcamp", ""),
    CAPABILITIES_NOT_EXISTS("400", "Some of the Capabilities are not registered", ""),
    CAPABILITY_TECHNOLOGIES_FOUND("200", "Capability with technologies found", ""),
    DUPLICATE_NAMES_CAPABILITY("404-7", "The names of the Capabilities cannot be the same", ""),
    TECHNOLOGY_NOT_EXISTS("400", " Some of the technologies are not registered.", ""),
    CAPABILITIES_DUPLICATES_IDS("400-8","Check the input data, it is trying to save the same capabilities",""),
    BOOTCAMP_CAPABILITY_CREATED("201-1", "Relations created successfully", ""),
    BOOTCAMP_CAPABILITY_FOUND("200","Capabilities by Bootcamps found",""),
    CAPABILITIES_FOUND("200","Capabilities found",""),
    BOOTCAMPS_CAPABILITIES_NOT_EXISTS("400-9"," The Capabilities not have or not found bootcamps associated." ,"" ),
    BOOTCAMPS_CAPABILITIES_MORE_ONE_RELATE("400-10","The Capabilities is found relate with others Bootcamps" ,"" ),
    BOOTCAMPS_FOUND("200","Bootcamps found",""),
    BOOTCAMPS_CAPABILITIES_DELETE("200"," The capabilities and bootcamps was delete successfully." ,"" );

    private final String code;
    private final String message;
    private final String param;

}
