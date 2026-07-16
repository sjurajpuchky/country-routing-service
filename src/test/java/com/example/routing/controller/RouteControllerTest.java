package com.example.routing.controller;

import com.example.routing.service.RouteNotFoundException;
import com.example.routing.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RouteControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RouteService routeService = new RouteService(() -> Collections.emptyList()) {
            @Override
            public List<String> findRoute(String origin, String destination) {
                if ("CZE".equals(origin) && "ITA".equals(destination)) {
                    return List.of("CZE", "AUT", "ITA");
                }
                throw new RouteNotFoundException("No land route exists");
            }
        };
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RouteController(routeService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void returnsRouteJson() throws Exception {
        mockMvc.perform(get("/routing/CZE/ITA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route[0]").value("CZE"))
                .andExpect(jsonPath("$.route[1]").value("AUT"))
                .andExpect(jsonPath("$.route[2]").value("ITA"));
    }

    @Test
    void returnsBadRequestWhenRouteDoesNotExist() throws Exception {
        mockMvc.perform(get("/routing/CZE/ISL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No land route exists"));
    }
}
