package com.tourplanner.model.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:entitytest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class EntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsUserTourAndTourLogRelations() {
        UserEntity user = new UserEntity();
        user.setUsername("entity-user");
        user.setPasswordHash("hashed-password");
        entityManager.persist(user);

        TourEntity tour = new TourEntity();
        tour.setUser(user);
        tour.setName("City Walk");
        tour.setDescription("A short city tour");
        tour.setFrom("Vienna");
        tour.setTo("Graz");
        tour.setTransportType(TransportType.HIKE);
        tour.setTourDistance(200.5);
        tour.setEstimatedTime(180);
        tour.setRouteInformation("{\"route\":\"demo\"}");
        tour.setImageFilenameOrReference("city-walk.png");
        entityManager.persist(tour);

        TourLogEntity tourLog = new TourLogEntity();
        tourLog.setTour(tour);
        tourLog.setDateTime(LocalDateTime.of(2026, 4, 27, 10, 30));
        tourLog.setComment("Good weather");
        tourLog.setDifficulty(3);
        tourLog.setTotalDistance(200.5);
        tourLog.setTotalTime(175);
        tourLog.setRating(4);
        entityManager.persist(tourLog);

        entityManager.flush();
        entityManager.clear();

        TourLogEntity savedTourLog = entityManager.find(TourLogEntity.class, tourLog.getId());

        assertNotNull(savedTourLog);
        assertNotNull(savedTourLog.getTour());
        assertEquals(tour.getId(), savedTourLog.getTour().getId());
        assertNotNull(savedTourLog.getTour().getUser());
        assertEquals(user.getId(), savedTourLog.getTour().getUser().getId());
    }
}
