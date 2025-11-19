package com.leo.asteroidalerting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import com.leo.asteroidalerting.dto.Asteroid;

@Data // automatically creates getters and setters
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NasaResponse {

    @JsonProperty("near_earth_objects")
    private Map<String, List<Asteroid>> nearEarthObjects;

    @JsonProperty("element_count")
    private Long totalAsteroids;
}
