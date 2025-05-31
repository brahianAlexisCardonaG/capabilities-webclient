package com.capabilities.project.infraestructure.entrypoints;

import com.capabilities.project.infraestructure.entrypoints.handler.CapabilityHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(CapabilityHandlerImpl capabilityHandler) {
        return RouterFunctions
                .route(POST("/capability"), capabilityHandler::create)
                .andRoute(GET("/capability"), capabilityHandler::getTechnologiesByCapabilitiesIds)
                .andRoute(POST("/capability/relate-technologies"),
                        capabilityHandler::createCapabilityRelateTechnologies)
                .andRoute(POST("/capability-bootcamp"),
                        capabilityHandler::createBootcampCapability)
                .andRoute(POST("/capability/by-bootcamp-ids"),
                        capabilityHandler::getCapabilitiesListByBootcampIds);
    }

}
