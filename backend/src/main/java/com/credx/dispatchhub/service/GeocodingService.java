package com.credx.dispatchhub.service;

import com.credx.dispatchhub.dto.response.GeocodeResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

/**
 * Forward geocoding backed by OpenStreetMap's Nominatim service, which is
 * free and needs no API key. Results are cached in memory: address strings
 * repeat often (riders re-type the same places) and Nominatim's usage policy
 * asks for at most 1 request/second, so the cache does double duty as
 * politeness and latency win. Swap `dispatchhub.geocoding.base-url` to point
 * at a self-hosted Nominatim or a commercial provider with the same API.
 */
@Service
public class GeocodingService {

    private static final int MAX_RESULTS = 5;
    private static final int MAX_CACHE_ENTRIES = 5_000;

    private final RestClient restClient;
    private final Map<String, List<GeocodeResult>> cache = new ConcurrentHashMap<>();

    public GeocodingService(@Value("${dispatchhub.geocoding.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                // Nominatim's usage policy requires an identifying User-Agent.
                .defaultHeader(HttpHeaders.USER_AGENT, "DispatchHub/0.1 (ride-dispatch demo)")
                .build();
    }

    public List<GeocodeResult> geocode(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase();
        if (normalized.length() < 3) {
            throw new IllegalArgumentException("Search text must be at least 3 characters");
        }

        if (cache.size() > MAX_CACHE_ENTRIES) {
            cache.clear();
        }

        return cache.computeIfAbsent(normalized, this::fetch);
    }

    private List<GeocodeResult> fetch(String query) {
        String uri = UriComponentsBuilder.fromPath("/search")
                .queryParam("format", "json")
                .queryParam("limit", MAX_RESULTS)
                .queryParam("q", query)
                .build()
                .toUriString();

        JsonNode results = restClient.get()
                .uri(uri)
                .retrieve()
                .body(JsonNode.class);

        if (results == null || !results.isArray()) {
            return List.of();
        }

        return StreamSupport.stream(results.spliterator(), false)
                .map(node -> GeocodeResult.builder()
                        .displayName(node.path("display_name").asText())
                        .lat(node.path("lat").asDouble())
                        .lng(node.path("lon").asDouble())
                        .build())
                .toList();
    }
}
