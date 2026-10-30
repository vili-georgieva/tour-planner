package com.tourplanner.controller;

import com.tourplanner.dto.TourCreateRequestDto;
import com.tourplanner.dto.TourListResponseDto;
import com.tourplanner.dto.TourResponseDto;
import com.tourplanner.dto.TourUpdateRequestDto;
import com.tourplanner.dto.SearchResponseDto;
import com.tourplanner.service.TourService;
import com.tourplanner.service.SearchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tours")
@CrossOrigin(origins = "http://localhost:4200") // frontend port for CORS
public class TourController {

    private final TourService tourService;
    private final SearchService searchService;

    public TourController(TourService tourService, SearchService searchService) {
        this.tourService = tourService;
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<TourResponseDto> createTour(
        HttpServletRequest request,
        @Valid @RequestBody TourCreateRequestDto body
    ) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(tourService.createTour(userId, body));
    }

    @GetMapping
    public ResponseEntity<TourListResponseDto> getTours(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(tourService.getTours(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourResponseDto> updateTour(
        HttpServletRequest request,
        @PathVariable Long id,
        @Valid @RequestBody TourUpdateRequestDto body
    ) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(tourService.updateTour(userId, id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        tourService.deleteTour(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponseDto> searchTours(
        HttpServletRequest request,
        @RequestParam @NotBlank String q
    ) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(searchService.search(userId, q));
    }
}
