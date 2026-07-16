package com.example.routing.controller;

import com.example.routing.model.RouteResponse;
import com.example.routing.service.RouteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/routing")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/{origin}/{destination}")
    public RouteResponse route(@PathVariable String origin, @PathVariable String destination) {
        return new RouteResponse(routeService.findRoute(origin, destination));
    }
}
