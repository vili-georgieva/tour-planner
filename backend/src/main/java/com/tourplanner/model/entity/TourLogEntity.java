package com.tourplanner.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_logs")
@Getter
@Setter
@NoArgsConstructor
public class TourLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TourEntity tour;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column
    private String comment;

    @Column(nullable = false)
    private Integer difficulty;

    @Column(name = "total_distance", nullable = false)
    private Double totalDistance;

    @Column(name = "total_time", nullable = false)
    private Integer totalTime;

    @Column(nullable = false)
    private Integer rating;

}
