package com.leo.asteroidalerting.service;


import com.leo.asteroidalerting.event.AsteroidCollisionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.leo.asteroidalerting.client.NasaClient;
import com.leo.asteroidalerting.dto.Asteroid;

import java.time.LocalDate;
import java.util.List;

@Service 
@Slf4j
public class AsteroidAlertingService {

    private final NasaClient nasaClient;
    private final KafkaTemplate<String, AsteroidCollisionEvent> kafkaTemplate;

    @Autowired
    public AsteroidAlertingService (NasaClient nasaClient, KafkaTemplate<String, AsteroidCollisionEvent> kafkaTemplate) {
        this.nasaClient = nasaClient;
        this.kafkaTemplate = kafkaTemplate;
    }


    public void alert() {
        log.info("Alerting service called");

        // get current and future time limits
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = LocalDate.now().plusDays(1);


        // call nasa api
        log.info("Getting asteroid list for dates: {} to {} ", startDate, endDate);
        final List<Asteroid> asteroidList = nasaClient.getAsteroids(startDate, endDate);

        log.info("Received {} asteroids from NASA API", asteroidList.size());

        // if any hazardous things were detected, send an alert
        final List<Asteroid> dangerousAsteroids =   asteroidList.stream()
                .filter(Asteroid::isPotentiallyHazardous)
                .toList();
        // create alert and put it on kafka topic
        final List<AsteroidCollisionEvent> asteroidCollisionEventList =
                createEventListOfDangerousAsteroids(dangerousAsteroids);

        log.info("Sending {} potentially dangerous asteroid collision events on kafka topic", asteroidCollisionEventList.size());
        asteroidCollisionEventList.forEach(event -> {
            kafkaTemplate.send("asteroid-alert", event);
            log.info("Asteroid alert sent to Kafka topic: {}", event);
        });
    }

    public List<AsteroidCollisionEvent> createEventListOfDangerousAsteroids(final List<Asteroid> dangerousAsteroids) {
        return dangerousAsteroids.stream()
                .map(asteroid -> {
                        return AsteroidCollisionEvent.builder() // look for what this is
                                .asteroidName(asteroid.getName())
                                .closeApproachDate(LocalDate.parse(asteroid.getCloseApproachData().getFirst().getCloseApproachDate().toString()))
                                .missDistanceKilometers(asteroid.getCloseApproachData().getFirst().getMissDistance().getKilometers())
                                .estimatedDiameterAvgMeters(asteroid.getEstimatedDiameter().getMeters().getMinDiameter() +
                                        asteroid.getEstimatedDiameter().getMeters().getMaxDiameter() / 2)
                                .build();
                })
                .toList();
    }

}