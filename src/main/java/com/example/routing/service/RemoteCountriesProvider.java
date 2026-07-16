package com.example.routing.service;

import com.example.routing.model.Country;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Component
public class RemoteCountriesProvider implements CountryDataProvider {

    private static final TypeReference<List<Country>> COUNTRY_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI countriesUri;
    private volatile List<Country> cachedCountries;

    public RemoteCountriesProvider(
            ObjectMapper objectMapper,
            @Value("${countries.source-url:https://raw.githubusercontent.com/mledoze/countries/master/countries.json}") String sourceUrl
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.countriesUri = URI.create(sourceUrl);
    }

    @Override
    public List<Country> getCountries() {
        List<Country> countries = cachedCountries;
        if (countries == null) {
            synchronized (this) {
                countries = cachedCountries;
                if (countries == null) {
                    countries = fetchCountries();
                    cachedCountries = countries;
                }
            }
        }
        return countries;
    }

    private List<Country> fetchCountries() {
        HttpRequest request = HttpRequest.newBuilder(countriesUri)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new CountryDataException("Failed to load country data: HTTP " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), COUNTRY_LIST);
        } catch (IOException e) {
            throw new CountryDataException("Failed to parse country data", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CountryDataException("Interrupted while loading country data", e);
        }
    }
}
