package com.capabilities.project.domain.exception;

import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.infraestructure.entrypoints.util.error.ErrorDto;
import lombok.Getter;

@Getter
public class ProcessorException extends RuntimeException {
    private final TechnicalMessage technicalMessage;
    private final ErrorDto errorDto;

    public ProcessorException(Throwable cause, TechnicalMessage message) {
        super(cause);
        technicalMessage = message;
        this.errorDto = null;
    }

    public ProcessorException(String message,
                              TechnicalMessage technicalMessage) {

        super(message);
        this.technicalMessage = technicalMessage;
        this.errorDto = null;
    }

    public ProcessorException(String message, TechnicalMessage technicalMessage, ErrorDto errorDto) {
        super(message);
        this.technicalMessage = technicalMessage;
        this.errorDto = errorDto;
    }
}
