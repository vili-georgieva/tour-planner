package com.tourplanner.service;

import com.tourplanner.dto.SearchResponseDto;
import com.tourplanner.dto.TourResponseDto;
import com.tourplanner.model.entity.TourLogEntity;
import com.tourplanner.repository.TourLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final TourService tourService;
    private final TourLogRepository tourLogRepository;

    public SearchService(TourService tourService, TourLogRepository tourLogRepository) {
        this.tourService = tourService;
        this.tourLogRepository = tourLogRepository;
    }

    /**
     * Searches all tours of the given user.
     * Matches query (case-insensitive) against tour fields, log fields, and computed attributes.
     */
    public SearchResponseDto search(Long userId, String query) {
        String lowerQuery = query.trim().toLowerCase();

        List<TourResponseDto> allTours = tourService.getTours(userId).tours();

        List<TourResponseDto> matched = allTours.stream()
            .filter(t -> matchesTour(t, lowerQuery))
            .toList();

        return new SearchResponseDto(matched);
    }

    // Checks if the query matches any searchable field on the tour (including computed attributes).
    private boolean matchesTour(TourResponseDto t, String lowerQuery) {
        String searchText = String.join(" ",
            t.name(),
            t.description() != null ? t.description() : "",
            t.from(),
            t.to(),
            t.transportType().name(),
            String.valueOf(t.distanceKm()),
            String.valueOf(t.estimatedTimeMin()),
            String.valueOf(t.popularity()),
            t.childFriendliness()
        ).toLowerCase();
        
        if (searchText.contains(lowerQuery)) {
            return true;
        }

        List<TourLogEntity> logs = tourLogRepository.findAllByTourId(t.id());
        for (TourLogEntity log : logs) {
            String logText = String.join(" ",
                log.getDateTime() != null ? log.getDateTime().toString() : "",
                log.getComment() != null ? log.getComment() : "",
                String.valueOf(log.getDifficulty()),
                String.valueOf(log.getTotalDistance()),
                String.valueOf(log.getTotalTime()),
                String.valueOf(log.getRating())
            ).toLowerCase();
            if (logText.contains(lowerQuery)) {
                return true;
            }
        }
        return false;
    }
}
