package com.example.routing.service;

import com.example.routing.model.Country;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Service
public class RouteService {

    private final CountryDataProvider countryDataProvider;

    public RouteService(CountryDataProvider countryDataProvider) {
        this.countryDataProvider = countryDataProvider;
    }

    public List<String> findRoute(String origin, String destination) {
        String normalizedOrigin = normalizeCca3(origin);
        String normalizedDestination = normalizeCca3(destination);
        Map<String, Set<String>> bordersByCountry = buildBorderGraph(countryDataProvider.getCountries());

        if (!bordersByCountry.containsKey(normalizedOrigin) || !bordersByCountry.containsKey(normalizedDestination)) {
            throw new RouteNotFoundException("Origin or destination country is invalid");
        }
        if (normalizedOrigin.equals(normalizedDestination)) {
            return List.of(normalizedOrigin);
        }

        return bfs(normalizedOrigin, normalizedDestination, bordersByCountry);
    }

    private static String normalizeCca3(String value) {
        if (value == null || !value.matches("[A-Za-z]{3}")) {
            throw new RouteNotFoundException("Country code must be a three-letter cca3 value");
        }
        return value.toUpperCase(Locale.ROOT);
    }

    private static Map<String, Set<String>> buildBorderGraph(List<Country> countries) {
        Map<String, Set<String>> graph = new HashMap<>();
        Set<String> countryCodes = new HashSet<>();

        for (Country country : countries) {
            if (country.cca3() != null && country.cca3().matches("[A-Z]{3}")) {
                countryCodes.add(country.cca3());
                graph.putIfAbsent(country.cca3(), new LinkedHashSet<>());
            }
        }

        for (Country country : countries) {
            String code = country.cca3();
            if (!countryCodes.contains(code) || country.borders() == null) {
                continue;
            }
            for (String border : country.borders()) {
                if (countryCodes.contains(border)) {
                    graph.get(code).add(border);
                    graph.get(border).add(code);
                }
            }
        }
        return graph;
    }

    private static List<String> bfs(String origin, String destination, Map<String, Set<String>> graph) {
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> previous = new HashMap<>();

        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            String current = queue.remove();
            for (String neighbor : graph.getOrDefault(current, Set.of())) {
                if (!visited.add(neighbor)) {
                    continue;
                }
                previous.put(neighbor, current);
                if (neighbor.equals(destination)) {
                    return reconstructRoute(origin, destination, previous);
                }
                queue.add(neighbor);
            }
        }

        throw new RouteNotFoundException("No land route exists between origin and destination");
    }

    private static List<String> reconstructRoute(String origin, String destination, Map<String, String> previous) {
        List<String> route = new ArrayList<>();
        String current = destination;
        while (current != null) {
            route.add(0, current);
            if (current.equals(origin)) {
                return route;
            }
            current = previous.get(current);
        }
        throw new RouteNotFoundException("No land route exists between origin and destination");
    }
}
