package com.tourplanner.service;

import com.tourplanner.dto.ExportDataDto;
import com.tourplanner.dto.ImportRequestDto;
import com.tourplanner.dto.ImportResponseDto;
import com.tourplanner.dto.TourExportDto;
import com.tourplanner.dto.TourLogExportDto;
import com.tourplanner.exception.ServiceException;
import com.tourplanner.model.entity.TourEntity;
import com.tourplanner.model.entity.TourLogEntity;
import com.tourplanner.model.entity.UserEntity;
import com.tourplanner.repository.TourLogRepository;
import com.tourplanner.repository.TourRepository;
import com.tourplanner.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@Service
public class ImportExportService {

    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    public ImportExportService(TourRepository tourRepository,
                               TourLogRepository tourLogRepository,
                               UserRepository userRepository,
                               ImageStorageService imageStorageService) {
        this.tourRepository = tourRepository;
        this.tourLogRepository = tourLogRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
    }

    public ExportDataDto exportData(@NotNull Long userId) {
        List<TourEntity> tours = tourRepository.findAllByUserId(userId);
        List<TourExportDto> tourDtos = tours.stream().map(tour -> {
            List<TourLogEntity> logs = tourLogRepository.findAllByTourId(tour.getId());
            List<TourLogExportDto> logDtos = logs.stream().map(log -> new TourLogExportDto(
                log.getDateTime().toString(),
                log.getComment(),
                log.getDifficulty(),
                log.getTotalDistance(),
                log.getTotalTime(),
                log.getRating()
            )).toList();
            return new TourExportDto(
                tour.getName(),
                tour.getDescription(),
                tour.getFrom(),
                tour.getTo(),
                tour.getTransportType(),
                tour.getTourDistance(),
                tour.getEstimatedTime(),
                tour.getRouteInformation(),
                tour.getImageFilenameOrReference(),
                logDtos
            );
        }).toList();
        return new ExportDataDto(tourDtos);
    }

    public ImportResponseDto importData(@NotNull Long userId, @Valid @NotNull ImportRequestDto request) {
        // Use proxy reference to avoid extra DB lookup
        UserEntity user = userRepository.getReferenceById(userId);
        int importedTours = 0;
        int importedLogs = 0;

        for (TourExportDto tourDto : request.tours()) {
            TourEntity tour = new TourEntity();
            tour.setUser(user);
            tour.setName(tourDto.name());
            tour.setDescription(tourDto.description());
            tour.setFrom(tourDto.from());
            tour.setTo(tourDto.to());
            tour.setTransportType(tourDto.transportType());
            tour.setTourDistance(tourDto.distanceKm());
            tour.setEstimatedTime(tourDto.estimatedTimeMin());
            tour.setRouteInformation(tourDto.routeGeometry());
            
            // Resolve imported image ownership and collisions by duplicating the file
            String oldRef = tourDto.imageFilePath();
            if (oldRef != null && !oldRef.isBlank() && oldRef.contains("/")) {
                try {
                    int slashIdx = oldRef.indexOf("/");
                    Long oldUserId = Long.parseLong(oldRef.substring(0, slashIdx));
                    String oldFilename = oldRef.substring(slashIdx + 1);
                    
                    byte[] imageBytes = imageStorageService.loadImage(oldUserId, oldFilename);
                    String newRef = imageStorageService.storeImage(userId, oldFilename, imageBytes);
                    tour.setImageFilenameOrReference(newRef);
                } catch (Exception e) {
                    // Fallback to the old reference if copy fails (e.g. image file not found)
                    tour.setImageFilenameOrReference(oldRef);
                }
            } else {
                tour.setImageFilenameOrReference(oldRef);
            }

            TourEntity savedTour = tourRepository.save(tour);
            importedTours++;

            for (TourLogExportDto logDto : tourDto.logs()) {
                TourLogEntity log = new TourLogEntity();
                log.setTour(savedTour);
                log.setDateTime(parseDateTime(logDto.loggedAt()));
                log.setComment(logDto.comment());
                log.setDifficulty(logDto.difficulty());
                log.setTotalDistance(logDto.totalDistanceKm());
                log.setTotalTime(logDto.totalTimeMin());
                log.setRating(logDto.rating());
                tourLogRepository.save(log);
                importedLogs++;
            }
        }

        return new ImportResponseDto(importedTours, importedLogs);
    }

    private LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            throw new ServiceException("Invalid date format: " + value);
        }
    }
}
