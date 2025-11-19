package com.leo.asteroidalerting.client;

import com.leo.asteroidalerting.dto.Asteroid;
import com.leo.asteroidalerting.dto.NasaResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service 
public class NasaClient {
    // these two values are defined in application.properties
    @Value("${nasa-neo-api-url}")
    private String nasaNeoApiUrl;

    @Value("${nasa-api-key}")
    private String nasaApiKey;

    // get asteroid function
    // calls the api, assigns it to NasaNeoResponse DTO
    // from that DTO, use a stream to make a list of Asteroid DTOs
    public List<Asteroid> getAsteroids(LocalDate startDate, LocalDate endDate) {
        // we use this to make calls to the nasa api
        final RestTemplate restTemplate = new RestTemplate();

        // store response in DTO
        // getForObject performs a GET request and maps the response body to a NasaResponse      Object
        final NasaResponse nasaResponse =
                restTemplate.getForObject(getUrl(startDate, endDate), NasaResponse.class);

        List<Asteroid> asteroidList = new ArrayList<>();
        if (nasaResponse != null) {
            asteroidList.addAll(nasaResponse.getNearEarthObjects().values().stream().flatMap(List::stream).toList());
        }

        return asteroidList;
    }

    public String getUrl(LocalDate startDate, LocalDate endDate) {
        // builds a url given a base url (defined in application.properties) and queryParams
        // saves the hassle of building the string ourselves
        return UriComponentsBuilder.fromHttpUrl(nasaNeoApiUrl)
                .queryParam("start_date", startDate)
                .queryParam("end_date", endDate)
                .queryParam("api_key", nasaApiKey)
                .toUriString();
    }
}