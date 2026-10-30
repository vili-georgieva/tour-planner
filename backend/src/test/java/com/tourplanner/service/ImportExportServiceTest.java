package com.tourplanner.service;

import com.tourplanner.dto.ExportDataDto;
import com.tourplanner.dto.ImportRequestDto;
import com.tourplanner.dto.ImportResponseDto;
import com.tourplanner.dto.RouteResponseDto;
import com.tourplanner.dto.TourCreateRequestDto;
import com.tourplanner.dto.TourExportDto;
import com.tourplanner.dto.TourLogCreateRequestDto;
import com.tourplanner.dto.TourLogExportDto;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:importexporttest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "tourplanner.ors.api-key=test-key",
    "tourplanner.image.storage-path=./test-images"
})
@Transactional
class ImportExportServiceTest {

    @Autowired
    private ImportExportService importExportService;

    @Autowired
    private TourService tourService;

    @Autowired
    private TourLogService tourLogService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private RouteService routeService;

    @MockitoBean
    private ImageStorageService imageStorageService;

    private Long userId;

    @BeforeEach
    void setup() {
        Mockito.when(routeService.getRoute(Mockito.any()))
            .thenReturn(new RouteResponseDto(10.5, 90, "[]", null, null, null));

        UserEntity user = new UserEntity();
        user.setUsername("export-user");
        user.setPasswordHash("secret");
        userId = userRepository.save(user).getId();
    }

    // Export returns all tours and their logs for the user
    @Test
    void exportDataReturnsAllToursAndLogsForUser() {
        TourResponseDto tour = tourService.createTour(userId,
            new TourCreateRequestDto("Export Tour", "desc", "A", "B", TransportType.HIKE, null));
        tourLogService.createLog(userId,
            new TourLogCreateRequestDto(tour.id(), "2024-01-01T10:00:00", "log1", 2, 5.0, 60, 4));
        tourLogService.createLog(userId,
            new TourLogCreateRequestDto(tour.id(), "2024-01-02T10:00:00", "log2", 3, 8.0, 90, 3));

        ExportDataDto result = importExportService.exportData(userId);

        assertEquals(1, result.tours().size());
        assertEquals("Export Tour", result.tours().get(0).name());
        assertEquals(2, result.tours().get(0).logs().size());
    }

    // Import creates tours and logs in the database
    @Test
    void importDataSavesToursAndLogs() {
        TourLogExportDto logDto = new TourLogExportDto("2024-03-01T12:00:00", "imported log", 2, 7.5, 80, 5);
        TourExportDto tourDto = new TourExportDto("Imported Tour", "imported desc", "X", "Y",
            TransportType.BIKE, 20.0, 120, "[]", null, List.of(logDto));
        ImportRequestDto request = new ImportRequestDto(List.of(tourDto));

        ImportResponseDto result = importExportService.importData(userId, request);

        assertEquals(1, result.importedTours());
        assertEquals(1, result.importedLogs());

        // Verify data is readable
        List<TourResponseDto> tours = tourService.getTours(userId).tours();
        assertEquals(1, tours.size());
        assertEquals("Imported Tour", tours.get(0).name());
    }
}
