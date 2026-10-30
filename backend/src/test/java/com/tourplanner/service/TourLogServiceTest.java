package com.tourplanner.service;

import com.tourplanner.dto.RouteResponseDto;
import com.tourplanner.dto.TourCreateRequestDto;
import com.tourplanner.dto.TourLogCreateRequestDto;
import com.tourplanner.dto.TourLogListResponseDto;
import com.tourplanner.dto.TourLogResponseDto;
import com.tourplanner.dto.TourLogUpdateRequestDto;
import com.tourplanner.dto.TourResponseDto;
import com.tourplanner.exception.ServiceException;
import com.tourplanner.model.entity.TransportType;
import com.tourplanner.model.entity.UserEntity;
import com.tourplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:tourlogservicetest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "tourplanner.ors.api-key=test-key",
    "tourplanner.image.storage-path=./test-images"
})
@Transactional
class TourLogServiceTest {

    @Autowired
    private TourService tourService;

    @Autowired
    private TourLogService tourLogService;

    @Autowired
    private UserRepository userRepository;

    // Mock ORS calls so tests do not make real HTTP requests
    @MockitoBean
    private RouteService routeService;

    private Long userId;
    private Long otherUserId;
    private Long tourId;

    @BeforeEach
    void setup() {
        Mockito.when(routeService.getRoute(Mockito.any()))
            .thenReturn(new RouteResponseDto(10.5, 90, "[]", null, null, null));
        UserEntity user = new UserEntity();
        user.setUsername("log-owner");
        user.setPasswordHash("secret");
        userId = userRepository.save(user).getId();

        UserEntity other = new UserEntity();
        other.setUsername("log-other");
        other.setPasswordHash("secret");
        otherUserId = userRepository.save(other).getId();

        TourResponseDto tour = tourService.createTour(userId, new TourCreateRequestDto(
            "Test Tour", "Desc", "A", "B", TransportType.HIKE, null));
        tourId = tour.id();
    }

    private TourLogCreateRequestDto validLogRequest() {
        return new TourLogCreateRequestDto(tourId, "2024-06-01T10:00:00", "Nice hike", 3, 12.5, 120, 4);
    }

    // Create log for owned tour
    @Test
    void createLogReturnsDtoWithId() {
        TourLogResponseDto result = tourLogService.createLog(userId, validLogRequest());

        assertNotNull(result.id());
        assertEquals(tourId, result.tourId());
        assertEquals("Nice hike", result.comment());
        assertEquals(3, result.difficulty());
        assertEquals(12.5, result.totalDistanceKm());
        assertEquals(120, result.totalTimeMin());
        assertEquals(4, result.rating());
    }

    // Creating log for another user's tour is rejected
    @Test
    void createLogFailsForNonOwnedTour() {
        assertThrows(ServiceException.class,
            () -> tourLogService.createLog(otherUserId, validLogRequest()));
    }

    // Get logs returns correct list for owned tour
    @Test
    void getLogsReturnsLogsForOwnedTour() {
        tourLogService.createLog(userId, validLogRequest());
        tourLogService.createLog(userId, new TourLogCreateRequestDto(tourId, "2024-06-02T08:00:00", "Second log", 2, 5.0, 60, 5));

        TourLogListResponseDto result = tourLogService.getLogs(userId, tourId);

        assertEquals(2, result.logs().size());
    }

    // Accessing logs of another user's tour is rejected
    @Test
    void getLogsFailsWhenTourNotOwned() {
        assertThrows(ServiceException.class,
            () -> tourLogService.getLogs(otherUserId, tourId));
    }

    // Update log changes fields
    @Test
    void updateLogChangesFields() {
        TourLogResponseDto created = tourLogService.createLog(userId, validLogRequest());
        TourLogUpdateRequestDto updateRequest = new TourLogUpdateRequestDto(
            "2024-06-05T14:00:00", "Updated comment", 5, 20.0, 200, 2);

        TourLogResponseDto updated = tourLogService.updateLog(userId, created.id(), updateRequest);

        assertEquals("Updated comment", updated.comment());
        assertEquals(5, updated.difficulty());
        assertEquals(20.0, updated.totalDistanceKm());
        assertEquals(200, updated.totalTimeMin());
        assertEquals(2, updated.rating());
    }

    // Update fails when log belongs to another user's tour
    @Test
    void updateLogFailsWhenNotOwner() {
        TourLogResponseDto created = tourLogService.createLog(userId, validLogRequest());

        assertThrows(ServiceException.class,
            () -> tourLogService.updateLog(otherUserId, created.id(),
                new TourLogUpdateRequestDto("2024-06-05T14:00:00", "X", 1, 1.0, 10, 1)));
    }

    // Delete removes the log
    @Test
    void deleteLogRemovesItFromList() {
        TourLogResponseDto created = tourLogService.createLog(userId, validLogRequest());

        tourLogService.deleteLog(userId, created.id());

        assertEquals(0, tourLogService.getLogs(userId, tourId).logs().size());
    }

    // Delete fails when log belongs to another user's tour
    @Test
    void deleteLogFailsWhenNotOwner() {
        TourLogResponseDto created = tourLogService.createLog(userId, validLogRequest());

        assertThrows(ServiceException.class,
            () -> tourLogService.deleteLog(otherUserId, created.id()));
    }

    // Deleting a tour cascades to its logs (DB-level via @OnDelete)
    @Test
    void deletingTourRemovesItsLogs() {
        tourLogService.createLog(userId, validLogRequest());

        // Tour deletion cascades to logs at DB level (@OnDelete CASCADE on FK)
        tourService.deleteTour(userId, tourId);

        // Verify the tour itself is gone
        assertEquals(0, tourService.getTours(userId).tours().size());
    }

    // Invalid date-time format is rejected
    @Test
    void createLogRejectsInvalidDateTimeFormat() {
        TourLogCreateRequestDto badRequest = new TourLogCreateRequestDto(
            tourId, "not-a-date", "comment", 3, 12.5, 120, 4);

        assertThrows(ServiceException.class,
            () -> tourLogService.createLog(userId, badRequest));
    }
}
