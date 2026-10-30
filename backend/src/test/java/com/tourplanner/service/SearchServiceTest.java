package com.tourplanner.service;

import com.tourplanner.dto.RouteResponseDto;
import com.tourplanner.dto.SearchResponseDto;
import com.tourplanner.dto.TourCreateRequestDto;
import com.tourplanner.dto.TourLogCreateRequestDto;
import com.tourplanner.dto.TourResponseDto;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:searchservicetest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "tourplanner.ors.api-key=test-key",
    "tourplanner.image.storage-path=./test-images"
})
@Transactional
class SearchServiceTest {

    @Autowired
    private TourService tourService;

    @Autowired
    private TourLogService tourLogService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private RouteService routeService;

    private Long userId;

    @BeforeEach
    void setup() {
        Mockito.when(routeService.getRoute(Mockito.any()))
            .thenReturn(new RouteResponseDto(15.0, 90, "[]", null, null, null));

        UserEntity user = new UserEntity();
        user.setUsername("search-user");
        user.setPasswordHash("secret");
        userId = userRepository.save(user).getId();
    }

    // Search by tour name matches correctly
    @Test
    void searchByNameReturnsMatchingTour() {
        tourService.createTour(userId, new TourCreateRequestDto("Alpine Hike", "Mountain tour", "Vienna", "Salzburg", TransportType.HIKE, null));
        tourService.createTour(userId, new TourCreateRequestDto("City Bike", "Urban tour", "Berlin", "Munich", TransportType.BIKE, null));

        SearchResponseDto result = searchService.search(userId, "alpine");

        assertEquals(1, result.tours().size());
        assertEquals("Alpine Hike", result.tours().get(0).name());
    }

    // Search by transport type returns matching tours
    @Test
    void searchByTransportTypeReturnsTours() {
        tourService.createTour(userId, new TourCreateRequestDto("Hike Tour", "desc", "A", "B", TransportType.HIKE, null));
        tourService.createTour(userId, new TourCreateRequestDto("Bike Tour", "desc", "C", "D", TransportType.BIKE, null));

        SearchResponseDto result = searchService.search(userId, "bike");

        assertEquals(1, result.tours().size());
        assertEquals("Bike Tour", result.tours().get(0).name());
    }

    // Search returns empty when no tour matches
    @Test
    void searchReturnsEmptyWhenNoMatch() {
        tourService.createTour(userId, new TourCreateRequestDto("Hike Tour", "desc", "A", "B", TransportType.HIKE, null));

        SearchResponseDto result = searchService.search(userId, "nonexistentxyz");

        assertTrue(result.tours().isEmpty());
    }

    // Popularity equals number of logs
    @Test
    void popularityEqualsLogCount() {
        TourResponseDto tour = tourService.createTour(userId, new TourCreateRequestDto("Pop Tour", "desc", "A", "B", TransportType.HIKE, null));
        tourLogService.createLog(userId, new TourLogCreateRequestDto(tour.id(), "2024-01-01T10:00:00", "comment", 2, 5.0, 60, 4));
        tourLogService.createLog(userId, new TourLogCreateRequestDto(tour.id(), "2024-01-02T10:00:00", "comment2", 3, 8.0, 90, 3));

        List<TourResponseDto> tours = tourService.getTours(userId).tours();

        assertEquals(2, tours.get(0).popularity());
    }

    // childFriendliness is Unknown when no logs
    @Test
    void childFriendlinessIsUnknownWithNoLogs() {
        TourResponseDto tour = tourService.createTour(userId, new TourCreateRequestDto("Empty Tour", "desc", "A", "B", TransportType.HIKE, null));

        List<TourResponseDto> tours = tourService.getTours(userId).tours();

        assertEquals("Unknown", tours.get(0).childFriendliness());
    }

    // childFriendliness is Yes for easy logs
    @Test
    void childFriendlinessIsYesForEasyLogs() {
        TourResponseDto tour = tourService.createTour(userId, new TourCreateRequestDto("Easy Tour", "desc", "A", "B", TransportType.HIKE, null));
        // difficulty=1, distance=5km, time=60min -> should be "Yes"
        tourLogService.createLog(userId, new TourLogCreateRequestDto(tour.id(), "2024-01-01T10:00:00", "easy", 1, 5.0, 60, 5));

        List<TourResponseDto> tours = tourService.getTours(userId).tours();

        assertEquals("Yes", tours.get(0).childFriendliness());
    }

    // childFriendliness is No for hard logs
    @Test
    void childFriendlinessIsNoForHardLogs() {
        TourResponseDto tour = tourService.createTour(userId, new TourCreateRequestDto("Hard Tour", "desc", "A", "B", TransportType.HIKE, null));
        // difficulty=5, distance=50km, time=600min -> should be "No"
        tourLogService.createLog(userId, new TourLogCreateRequestDto(tour.id(), "2024-01-01T10:00:00", "hard", 5, 50.0, 600, 1));

        List<TourResponseDto> tours = tourService.getTours(userId).tours();

        assertEquals("No", tours.get(0).childFriendliness());
    }

    // Search only returns current user's tours
    @Test
    void searchReturnsOnlyOwnUserTours() {
        UserEntity other = new UserEntity();
        other.setUsername("other-search-user");
        other.setPasswordHash("secret");
        Long otherId = userRepository.save(other).getId();

        tourService.createTour(userId, new TourCreateRequestDto("My Tour", "desc", "A", "B", TransportType.HIKE, null));
        tourService.createTour(otherId, new TourCreateRequestDto("My Tour", "desc", "A", "B", TransportType.HIKE, null));

        SearchResponseDto result = searchService.search(userId, "my tour");

        assertEquals(1, result.tours().size());
    }

    // childFriendliness is Moderate for medium-difficulty logs
    @Test
    void childFriendlinessIsModerateForMediumLogs() {
        TourResponseDto tour = tourService.createTour(userId, new TourCreateRequestDto("Medium Tour", "desc", "A", "B", TransportType.HIKE, null));
        // difficulty=3, distance=15km, time=150min -> between Yes and No thresholds
        tourLogService.createLog(userId, new TourLogCreateRequestDto(tour.id(), "2024-01-01T10:00:00", "moderate hike", 3, 15.0, 150, 3));

        List<TourResponseDto> tours = tourService.getTours(userId).tours();

        assertEquals("Moderate", tours.get(0).childFriendliness());
    }

    // Search by log comment returns matching tour
    @Test
    void searchByLogCommentReturnsTour() {
        TourResponseDto tour = tourService.createTour(userId, new TourCreateRequestDto("Hidden Tour", "desc", "A", "B", TransportType.HIKE, null));
        tourLogService.createLog(userId, new TourLogCreateRequestDto(tour.id(), "2024-01-01T10:00:00", "unique log text", 1, 5.0, 60, 5));

        SearchResponseDto result = searchService.search(userId, "unique");

        assertEquals(1, result.tours().size());
        assertEquals("Hidden Tour", result.tours().get(0).name());
    }
}
