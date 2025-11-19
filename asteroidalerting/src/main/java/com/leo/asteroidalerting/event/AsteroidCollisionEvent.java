package com.leo.asteroidalerting.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AsteroidCollisionEvent {
    // Properties we want to include in the notification
    // AKA, what's useful for the receiver to know
    private String asteroidName;
    private LocalDate closeApproachDate;
    private String missDistanceKilometers;
    private double estimatedDiameterAvgMeters;
}
