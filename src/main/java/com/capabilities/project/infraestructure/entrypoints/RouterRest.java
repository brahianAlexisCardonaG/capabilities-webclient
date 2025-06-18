package com.capabilities.project.infraestructure.entrypoints;

import com.capabilities.project.infraestructure.entrypoints.dto.BootcampCapabilityDto;
import com.capabilities.project.infraestructure.entrypoints.dto.CapabilityDto;
import com.capabilities.project.infraestructure.entrypoints.handler.CapabilityHandlerImpl;
import com.capabilities.project.infraestructure.entrypoints.util.response.ApiResponseBase;
import com.capabilities.project.infraestructure.entrypoints.util.response.ApiResponseBaseMap;
import com.capabilities.project.infraestructure.entrypoints.util.response.capability.ApiCapabilityTechnologyResponseList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.capabilities.project.infraestructure.entrypoints.util.Constants.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
@Tag(name = "Capability", description = "Capabilities API")
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = PATH_GET_TECHNOLOGIES_BY_IDS_CAPABILITY,
                    produces = { "application/json" },
                    method = org.springframework.web.bind.annotation.RequestMethod.GET,
                    beanClass = CapabilityHandlerImpl.class,
                    beanMethod = "getTechnologiesByCapabilitiesIds",
                    operation = @Operation(
                            operationId = "getTechnologiesByCapabilitiesIds",
                            summary = "Get technologies by capability IDs",
                            tags = { "Endpoints Capabilities" },
                            security = @SecurityRequirement(name = "BearerAuth"),
                            parameters = {
                                    @io.swagger.v3.oas.annotations.Parameter(
                                            in = ParameterIn.QUERY,
                                            name = "capabilityIds",
                                            description = "Comma-separated list of capability IDs",
                                            example = "1,2,3",
                                            required = true,
                                            schema = @Schema(type = "string")
                                    ),
                                    @io.swagger.v3.oas.annotations.Parameter(
                                            in = ParameterIn.QUERY,
                                            name = "order",
                                            description = "Sort order (asc, desc, tech)",
                                            example = "asc",
                                            required = false,
                                            schema = @Schema(type = "string", defaultValue = "asc")
                                    ),
                                    @io.swagger.v3.oas.annotations.Parameter(
                                            in = ParameterIn.QUERY,
                                            name = "rows",
                                            description = "Number of records to return",
                                            example = "4",
                                            required = false,
                                            schema = @Schema(type = "integer", defaultValue = "4")
                                    ),
                                    @io.swagger.v3.oas.annotations.Parameter(
                                            in = ParameterIn.QUERY,
                                            name = "skip",
                                            description = "Number of records to skip",
                                            example = "0",
                                            required = false,
                                            schema = @Schema(type = "integer", defaultValue = "0")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Technologies found",
                                            content = @Content(schema = @Schema(implementation = ApiCapabilityTechnologyResponseList.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid query parameters"
                                    ),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized")
                            }
                    )
            ),
            @RouterOperation(
                    path = PATH_POST_CREATE_RELATE_CAPABILITY_TECHNOLOGY,
                    produces = { "application/json" },
                    method = org.springframework.web.bind.annotation.RequestMethod.POST,
                    beanClass = CapabilityHandlerImpl.class,
                    beanMethod = "createCapabilityRelateTechnologies",
                    operation = @Operation(
                            operationId = "createCapabilityRelateTechnologies",
                            summary = "Create relationship between a capability and technologies",
                            tags = { "Endpoints Capabilities" },
                            security = @SecurityRequirement(name = "BearerAuth"),
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CapabilityDto.class)))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Relationship created successfully",
                                            content = @Content(schema = @Schema(implementation = ApiCapabilityTechnologyResponseList.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Validation error in request body"
                                    ),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized")
                            }
                    )
            ),
            @RouterOperation(
                    path = PATH_POST_CREATE_RELATE_BOOTCAMP_CAPABILITY,
                    produces = { "application/json" },
                    method = org.springframework.web.bind.annotation.RequestMethod.POST,
                    beanClass = CapabilityHandlerImpl.class,
                    beanMethod = "createBootcampCapability",
                    operation = @Operation(
                            operationId = "createBootcampCapability",
                            summary = "Associate a list of capabilities to a Bootcamp",
                            tags = { "Endpoints for webclients" },
                            security = @SecurityRequirement(name = "BearerAuth"),
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = BootcampCapabilityDto.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Bootcamp-capabilities association created",
                                            content = @Content(schema = @Schema(implementation = ApiResponseBase.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid parameters"
                                    ),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized")
                            }
                    )
            ),
            @RouterOperation(
                    path = PATH_GET_CAPABILITIES_BY_IDS_BOOTCAMPS,
                    produces = { "application/json" },
                    method = org.springframework.web.bind.annotation.RequestMethod.GET,
                    beanClass = CapabilityHandlerImpl.class,
                    beanMethod = "getCapabilitiesListByBootcampIds",
                    operation = @Operation(
                            operationId = "getCapabilitiesListByBootcampIds",
                            summary = "Get capabilities by Bootcamp IDs",
                            tags = { "Endpoints for webclients" },
                            security = @SecurityRequirement(name = "BearerAuth"),
                            parameters = {
                                    @io.swagger.v3.oas.annotations.Parameter(
                                            in = ParameterIn.QUERY,
                                            name = "bootcampIds",
                                            description = "Comma-separated list of Bootcamp IDs",
                                            example = "10,20,30",
                                            required = true,
                                            schema = @Schema(type = "string")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Capabilities found for each Bootcamp",
                                            content = @Content(schema = @Schema(implementation = ApiResponseBaseMap.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid query parameters"
                                    ),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized")
                            }
                    )
            ),
            @RouterOperation(
                    path = PATH_GET_CAPABILITIES_BY_IDS,
                    produces = { "application/json" },
                    method = org.springframework.web.bind.annotation.RequestMethod.GET,
                    beanClass = CapabilityHandlerImpl.class,
                    beanMethod = "getCapabilitiesByIds",
                    operation = @Operation(
                            operationId = "getCapabilitiesByIds",
                            summary = "Get capabilities by their IDs",
                            tags = { "Endpoints for webclients" },
                            security = @SecurityRequirement(name = "BearerAuth"),
                            parameters = {
                                    @io.swagger.v3.oas.annotations.Parameter(
                                            in = ParameterIn.QUERY,
                                            name = "ids",
                                            description = "Comma-separated list of capability IDs",
                                            example = "5,6,7",
                                            required = true,
                                            schema = @Schema(type = "string")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Capabilities found",
                                            content = @Content(schema = @Schema(implementation = ApiResponseBase.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid query parameters"
                                    ),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized")
                            }
                    )
            ),
            @RouterOperation(
                    path = PATH_DELETE_RELATE_BOOTCAMPS_CAPABILITIES,
                    produces = {"application/json"},
                    method = org.springframework.web.bind.annotation.RequestMethod.DELETE,
                    beanClass = CapabilityHandlerImpl.class,
                    beanMethod = "deleteBootcampsCapabilities",
                    operation = @Operation(
                            operationId = "deleteBootcampsCapabilities",
                            summary = "Delete capabilities and relate with bootcamps by capability IDs",
                            tags = { "Endpoints for webclients" },
                            security = @SecurityRequirement(name = "BearerAuth"),
                            parameters = {
                                    @io.swagger.v3.oas.annotations.Parameter(
                                            in = ParameterIn.QUERY,
                                            name = "ids",
                                            description = "List of ids capability separated by commas",
                                            example = "1,2,3",
                                            required = true
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Capability and relate with bootcamps deleted successfully",
                                            content = @Content(schema = @Schema(implementation = ApiResponseBase.class))
                                    ),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(CapabilityHandlerImpl capabilityHandler) {
        return RouterFunctions
                .route(GET(PATH_GET_TECHNOLOGIES_BY_IDS_CAPABILITY), capabilityHandler::getTechnologiesByCapabilitiesIds)
                .andRoute(POST(PATH_POST_CREATE_RELATE_CAPABILITY_TECHNOLOGY), capabilityHandler::createCapabilityRelateTechnologies)
                .andRoute(POST(PATH_POST_CREATE_RELATE_BOOTCAMP_CAPABILITY), capabilityHandler::createBootcampCapability)
                .andRoute(GET(PATH_GET_CAPABILITIES_BY_IDS_BOOTCAMPS), capabilityHandler::getCapabilitiesListByBootcampIds)
                .andRoute(GET(PATH_GET_CAPABILITIES_BY_IDS), capabilityHandler::getCapabilitiesByIds)
                .andRoute(DELETE(PATH_DELETE_RELATE_BOOTCAMPS_CAPABILITIES), capabilityHandler::deleteBootcampsCapabilities);
    }
}