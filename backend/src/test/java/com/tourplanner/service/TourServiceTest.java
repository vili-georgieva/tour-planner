package com.tourplanner.service;

import com.tourplanner.dto.RouteResponseDto;
import com.tourplanner.dto.TourCreateRequestDto;
import com.tourplanner.dto.TourListResponseDto;
import com.tourplanner.dto.TourResponseDto;
import com.tourplanner.dto.TourUpdateRequestDto;
import com.tourplanner.exception.ServiceException;
import com.tourplanner.model.entity.TransportType;
import com.tourplanner.repository.UserRepository;
import com.tourplanner.model.entity.UserEntity;
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
    "spring.datasource.url=jdbc:h2:mem:tourservicetest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "tourplanner.ors.api-key=test-key",
    "tourplanner.image.storage-path=./test-images"
})
@Transactional
class TourServiceTest {

    @Autowired
    private TourService tourService;

    @Autowired
    private UserRepository userRepository;

    // Mock ORS calls so tests do not make real HTTP requests
    @MockitoBean
    private RouteService routeService;

    private Long userId;
    private Long otherUserId;

    @BeforeEach
    void createUsers() {
        Mockito.when(routeService.getRoute(Mockito.any()))
            .thenReturn(new RouteResponseDto(10.5, 90, "[]", null, null, null));
        UserEntity user = new UserEntity();
        user.setUsername("tour-owner");
        user.setPasswordHash("secret");
        userId = userRepository.save(user).getId();

        UserEntity other = new UserEntity();
        other.setUsername("tour-other");
        other.setPasswordHash("secret");
        otherUserId = userRepository.save(other).getId();
    }

    private TourCreateRequestDto validCreateRequest() {
        return new TourCreateRequestDto("Alpine Hike", "A mountain hike", "Vienna", "Salzburg", TransportType.HIKE, null);
    }

    // Create tour, verify persisted with correct owner
    @Test
    void createTourReturnsDtoWithId() {
        TourResponseDto result = tourService.createTour(userId, validCreateRequest());

        assertNotNull(result.id());
        assertEquals("Alpine Hike", result.name());
        assertEquals("Vienna", result.from());
        assertEquals("Salzburg", result.to());
        assertEquals(TransportType.HIKE, result.transportType());
    }

    // List shows only current user's tours
    @Test
    void getToursReturnsOnlyOwnedTours() {
        tourService.createTour(userId, validCreateRequest());
        tourService.createTour(otherUserId, new TourCreateRequestDto("Other Tour", "Other desc", "A", "B", TransportType.BIKE, null));

        TourListResponseDto result = tourService.getTours(userId);

        assertEquals(1, result.tours().size());
        assertEquals("Alpine Hike", result.tours().get(0).name());
    }

    // Empty list when user has no tours
    @Test
    void getToursReturnsEmptyListWhenNone() {
        TourListResponseDto result = tourService.getTours(userId);

        assertEquals(0, result.tours().size());
    }

    // Update owned tour
    @Test
    void updateTourChangesFields() {
        TourResponseDto created = tourService.createTour(userId, validCreateRequest());
        TourUpdateRequestDto updateRequest = new TourUpdateRequestDto("Updated Hike", "New desc", "Graz", "Linz", TransportType.RUNNING, null);

        TourResponseDto updated = tourService.updateTour(userId, created.id(), updateRequest);

        assertEquals("Updated Hike", updated.name());
        assertEquals("Graz", updated.from());
        assertEquals("Linz", updated.to());
        assertEquals(TransportType.RUNNING, updated.transportType());
    }

    // Update fails for another user's tour
    @Test
    void updateTourFailsWhenNotOwner() {
        TourResponseDto created = tourService.createTour(userId, validCreateRequest());

        assertThrows(ServiceException.class,
            () -> tourService.updateTour(otherUserId, created.id(), new TourUpdateRequestDto("X", "X", "X", "X", TransportType.BIKE, null)));
    }

    // Delete removes the tour
    @Test
    void deleteTourRemovesTourFromList() {
        TourResponseDto created = tourService.createTour(userId, validCreateRequest());

        tourService.deleteTour(userId, created.id());

        assertEquals(0, tourService.getTours(userId).tours().size());
    }

    // Delete fails for another user's tour
    @Test
    void deleteTourFailsWhenNotOwner() {
        TourResponseDto created = tourService.createTour(userId, validCreateRequest());

        assertThrows(ServiceException.class,
            () -> tourService.deleteTour(otherUserId, created.id()));
    }

    // Elevation profile data is stored when ORS returns it
    @Test
    void createTourSavesElevationProfileData() {
        Mockito.when(routeService.getRoute(Mockito.any()))
            .thenReturn(new RouteResponseDto(10.5, 90, "[]", "[[8.0,48.0,200],[8.1,48.1,350]]", 150.0, 50.0));

        TourResponseDto result = tourService.createTour(userId, validCreateRequest());

        assertNotNull(result.elevationProfile());
        assertEquals(150.0, result.ascentM());
        assertEquals(50.0, result.descentM());
    }
}
