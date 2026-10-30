package com.tourplanner.service;

import com.tourplanner.config.SystemStatusResponseFactory;
import com.tourplanner.dto.SystemStatusResponseDto;
import com.tourplanner.exception.LayerArchitectureException;
import com.tourplanner.model.entity.SystemStatusEntity;
import com.tourplanner.repository.SystemStatusRepository;
import org.springframework.stereotype.Service;

@Service
public class SystemStatusService {

    private final SystemStatusRepository systemStatusRepository;
    private final SystemStatusResponseFactory systemStatusResponseFactory;

    public SystemStatusService(
        SystemStatusRepository systemStatusRepository,
        SystemStatusResponseFactory systemStatusResponseFactory
    ) {
        this.systemStatusRepository = systemStatusRepository;
        this.systemStatusResponseFactory = systemStatusResponseFactory;
    }

    public SystemStatusResponseDto getStatus() {
        SystemStatusEntity entity = systemStatusRepository.getStatus();

        if (entity == null) {
            throw new LayerArchitectureException("System status could not be loaded.");
        }

        return systemStatusResponseFactory.fromEntity(entity);
    }
}