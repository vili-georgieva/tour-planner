package com.tourplanner.controller;

import com.tourplanner.dto.SystemStatusResponseDto;
import com.tourplanner.service.SystemStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system-status")
public class SystemStatusController {

    private final SystemStatusService systemStatusService;

    public SystemStatusController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping
    public ResponseEntity<SystemStatusResponseDto> getSystemStatus() {
        return ResponseEntity.ok(systemStatusService.getStatus());
    }
}