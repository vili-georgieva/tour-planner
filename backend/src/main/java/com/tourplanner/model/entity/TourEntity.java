package com.tourplanner.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
public class TourEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "from_location", nullable = false)
    private String from;

    @Column(name = "to_location", nullable = false)
    private String to;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false)
    private TransportType transportType;

    @Column(name = "tour_distance")
    private Double tourDistance;

    @Column(name = "estimated_time")
    private Integer estimatedTime;

    @Column(name = "route_information", columnDefinition = "TEXT")
    private String routeInformation;

    @Column(name = "image_filename_or_reference")
    private String imageFilenameOrReference;

    @Column(name = "elevation_profile", columnDefinition = "TEXT")
    private String elevationProfile;

    @Column(name = "ascent_m")
    private Double ascentM;

    @Column(name = "descent_m")
    private Double descentM;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY)
    private List<TourLogEntity> tourLogs = new ArrayList<>();

}
