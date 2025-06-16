package com.capabilities.project.infraestructure.entrypoints.util.validate;

import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.infraestructure.entrypoints.dto.CapabilityDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CapabilityValidationDto {

    public Mono<CapabilityDto> validateFieldNotNullOrBlank(CapabilityDto dto) {
        if (dto.getDescription() == null || dto.getName() == null  || dto.getTechnologyIds().isEmpty()) {
            return Mono.error(new BusinessException(TechnicalMessage.INVALID_PARAMETERS));
        }
        return Mono.just(dto);
    }


    public Mono<CapabilityDto> validateLengthWords(CapabilityDto dto) {
        if (dto.getName().length() > 50) {
            return Mono.error(new BusinessException(TechnicalMessage.NAME_TOO_LONG));
        }
        if (dto.getDescription().length() > 90) {
            return Mono.error(new BusinessException(TechnicalMessage.DESCRIPTION_TOO_LONG));
        }
        return Mono.just(dto);
    }

    public Mono<List<CapabilityDto>> validateNoDuplicateNames(List<CapabilityDto> dtoList) {
        Set<String> names = new HashSet<>();
        List<String> duplicatedNames = dtoList.stream()
                .map(CapabilityDto::getName)
                .filter(name -> !names.add(name)) // Si no se puede agregar al set, es duplicado
                .toList();

        if (!duplicatedNames.isEmpty()) {
            return Mono.error(new BusinessException(TechnicalMessage.DUPLICATE_NAMES_CAPABILITY));
        }

        return Mono.just(dtoList);
    }
}
