package com.example.routing.service;

import com.example.routing.model.Country;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouteServiceTest {

    private final RouteService routeService = new RouteService(() -> List.of(
            new Country("CZE", List.of("AUT", "POL")),
            new Country("AUT", List.of("CZE", "ITA")),
            new Country("ITA", List.of("AUT")),
            new Country("POL", List.of("CZE")),
            new Country("ISL", List.of())
    ));

    @Test
    void returnsReachableLandRoute() {
        assertThat(routeService.findRoute("CZE", "ITA"))
                .containsExactly("CZE", "AUT", "ITA");
    }

    @Test
    void rejectsUnreachableRoute() {
        assertThatThrownBy(() -> routeService.findRoute("CZE", "ISL"))
                .isInstanceOf(RouteNotFoundException.class)
                .hasMessageContaining("No land route");
    }

    @Test
    void rejectsInvalidCountryCode() {
        assertThatThrownBy(() -> routeService.findRoute("CZE", "XXX"))
                .isInstanceOf(RouteNotFoundException.class)
                .hasMessageContaining("invalid");
    }
}
