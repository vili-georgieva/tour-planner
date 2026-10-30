package com.tourplanner.service;

import com.tourplanner.dto.RouteRequestDto;
import com.tourplanner.dto.RouteResponseDto;
import com.tourplanner.dto.TourCreateRequestDto;
import com.tourplanner.dto.TourListResponseDto;
import com.tourplanner.dto.TourResponseDto;
import com.tourplanner.dto.TourUpdateRequestDto;
import com.tourplanner.exception.ServiceException;
import com.tourplanner.model.entity.TourEntity;
import com.tourplanner.model.entity.TourLogEntity;
import com.tourplanner.model.entity.UserEntity;
import com.tourplanner.repository.TourLogRepository;
import com.tourplanner.repository.TourRepository;
import com.tourplanner.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TourService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final RouteService routeService;
    private final TourLogRepository tourLogRepository;

    public TourService(TourRepository tourRepository, UserRepository userRepository, RouteService routeService, TourLogRepository tourLogRepository) {
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
        this.routeService = routeService;
        this.tourLogRepository = tourLogRepository;
    }

    public TourResponseDto createTour(Long userId, TourCreateRequestDto request) {
        // Use a proxy reference to avoid an extra DB lookup just for the FK (Foreign Key)
        UserEntity user = userRepository.getReferenceById(userId);

        TourEntity tour = new TourEntity();
        tour.setUser(user);
        tour.setName(request.name());
        tour.setDescription(request.description());
        tour.setFrom(request.from());
        tour.setTo(request.to());
        tour.setTransportType(request.transportType());
        tour.setImageFilenameOrReference(request.imageFilePath());

        // Retrieve route data from ORS and persist with the tour
        RouteResponseDto route = routeService.getRoute(
            new RouteRequestDto(request.from(), request.to(), request.transportType())
        );
        tour.setTourDistance(route.distanceKm());
        tour.setEstimatedTime(route.estimatedTimeMin());
        tour.setRouteInformation(route.routeGeometry());
        tour.setElevationProfile(route.elevationProfile());
        tour.setAscentM(route.ascentM());
        tour.setDescentM(route.descentM());

        log.info("Created tour for userId: {} with name: {}", userId, request.name());
        return toResponseDto(tourRepository.save(tour));
    }

    public TourListResponseDto getTours(Long userId) {
        List<TourEntity> tours = tourRepository.findAllByUserId(userId);
        List<TourResponseDto> dtos = tours.stream().map(this::toResponseDto).toList();
        return new TourListResponseDto(dtos);
    }

    public TourResponseDto updateTour(Long userId, Long tourId, TourUpdateRequestDto request) {
        TourEntity tour = tourRepository.findByIdAndUserId(tourId, userId)
            .orElseThrow(() -> {
                log.error("Failed to update tour. Tour ID {} not found for User ID {}", tourId, userId);
                return new ServiceException("Tour not found.");
            });

        tour.setName(request.name());
        tour.setDescription(request.description());
        tour.setFrom(request.from());
        tour.setTo(request.to());
        tour.setTransportType(request.transportType());
        tour.setImageFilenameOrReference(request.imageFilePath());

        // Re-retrieve route data when from/to/type changes
        RouteResponseDto route = routeService.getRoute(
            new RouteRequestDto(request.from(), request.to(), request.transportType())
        );
        tour.setTourDistance(route.distanceKm());
        tour.setEstimatedTime(route.estimatedTimeMin());
        tour.setRouteInformation(route.routeGeometry());
        tour.setElevationProfile(route.elevationProfile());
        tour.setAscentM(route.ascentM());
        tour.setDescentM(route.descentM());
        log.info("Updated tour for userId: {} with name: {}", userId, request.name());
        return toResponseDto(tourRepository.save(tour));
    }

    public void deleteTour(Long userId, Long tourId) {
        TourEntity tour = tourRepository.findByIdAndUserId(tourId, userId)
            .orElseThrow(() -> {
                log.error("Failed to delete tour. Tour ID {} not found for User ID {}", tourId, userId);
                return new ServiceException("Tour not found.");
            });
        // DB-level cascade on the FK in TourLogEntity removes related logs automatically
        tourRepository.delete(tour);
        log.info("Deleted tour ID {} for User ID {}", tourId, userId);
    }

    private TourResponseDto toResponseDto(TourEntity tour) {
        int popularity = tourLogRepository.countByTourId(tour.getId());
        String childFriendliness = computeChildFriendliness(tour.getId());
        return new TourResponseDto(
            tour.getId(),
            tour.getName(),
            tour.getDescription(),
            tour.getFrom(),
            tour.getTo(),
            tour.getTransportType(),
            tour.getTourDistance(),
            tour.getEstimatedTime(),
            tour.getRouteInformation(),
            tour.getImageFilenameOrReference(),
            popularity,
            childFriendliness,
            tour.getElevationProfile(),
            tour.getAscentM(),
            tour.getDescentM()
        );
    }

    /**
     * Derives child-friendliness from log data.
     * "Yes"      -> avg difficulty <= 2, avg distance <= 10 km, avg time <= 120 min
     * "No"       -> avg difficulty >= 4, or avg distance > 30 km, or avg time > 300 min
     * "Moderate" -> everything else
     * "Unknown"  -> no logs recorded yet
     */
    private String computeChildFriendliness(Long tourId) {
        List<TourLogEntity> logs = tourLogRepository.findAllByTourId(tourId);
        if (logs.isEmpty()) {
            return "Unknown";
        }
        double avgDifficulty = logs.stream().mapToInt(TourLogEntity::getDifficulty).average().orElse(0);
        double avgDistance = logs.stream().mapToDouble(TourLogEntity::getTotalDistance).average().orElse(0);
        double avgTime = logs.stream().mapToInt(TourLogEntity::getTotalTime).average().orElse(0);

        if (avgDifficulty >= 4 || avgDistance > 30 || avgTime > 300) {
            return "No";
        }
        if (avgDifficulty <= 2 && avgDistance <= 10 && avgTime <= 120) {
            return "Yes";
        }
        return "Moderate";
    }
}
