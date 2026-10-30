package com.tourplanner.config;

import com.tourplanner.dto.SystemStatusResponseDto;
import com.tourplanner.model.entity.SystemStatusEntity;
import org.springframework.stereotype.Component;

@Component
public class SystemStatusResponseFactory {

    // Factory Pattern: central DTO creation point for protocol traceability.
    public SystemStatusResponseDto fromEntity(SystemStatusEntity entity) {
        return new SystemStatusResponseDto(entity.getMessage());
    }
}