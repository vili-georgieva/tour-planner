package com.tourplanner.controller;

import com.tourplanner.dto.ExportDataDto;
import com.tourplanner.dto.ImportRequestDto;
import com.tourplanner.dto.ImportResponseDto;
import com.tourplanner.service.ImportExportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class ImportExportController {

    private final ImportExportService importExportService;
    private final ObjectMapper objectMapper;

    public ImportExportController(ImportExportService importExportService, ObjectMapper objectMapper) {
        this.importExportService = importExportService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTours(HttpServletRequest request) throws Exception {
        Long userId = (Long) request.getAttribute("userId");
        ExportDataDto data = importExportService.exportData(userId);
        byte[] json = objectMapper.writeValueAsBytes(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDisposition(
            ContentDisposition.attachment().filename("tourplanner-export.json").build()
        );
        return ResponseEntity.ok().headers(headers).body(json);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResponseDto> importTours(
        HttpServletRequest request,
        @Valid @RequestBody ImportRequestDto body
    ) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(importExportService.importData(userId, body));
    }
}
