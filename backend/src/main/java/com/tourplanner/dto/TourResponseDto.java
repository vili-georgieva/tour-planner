package com.tourplanner.dto;

import com.tourplanner.model.entity.TransportType;

public record TourResponseDto(
    Long id,
    String name,
    String description,
    String from,
    String to,
    TransportType transportType,
    Double distanceKm,
    Integer estimatedTimeMin,
    String routeGeometry,
    String imageFilePath,
    // computed: number of logs for this tour
    int popularity,
    // computed: child-friendliness derived from log difficulty, distance, time
    String childFriendliness,
    String elevationProfile,    // 3D coordinates [[lon,lat,elev],...] as JSON string (nullable)
    Double ascentM,             // total ascent in meters (nullable)
    Double descentM             // total descent in meters (nullable)
) {
}
