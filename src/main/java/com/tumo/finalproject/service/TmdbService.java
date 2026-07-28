package com.tumo.finalproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumo.finalproject.model.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class TmdbService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public TmdbService(@Value("${tmdb.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.themoviedb.org/3")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<Movie> searchMovies(String query) {
        String json = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("query", query)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseMovie(json);
    }

    private List<Movie> parseMovie(String json) {
        List<Movie> movies = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode results = root.get("results");

            if (results != null && results.isArray()) {
                for (JsonNode node : results) {
                    Movie movie = new Movie();
                    movie.setId(node.get("id").asInt());
                    movie.setTitle(node.has("title") && !node.get("title").isNull() ? node.get("title").asText() : "");
                    movie.setOverview(node.has("overview") && !node.get("overview").isNull() ? node.get("overview").asText() : "");
                    movie.setVoteAverage(node.has("vote_average") && !node.get("vote_average").isNull() ? node.get("vote_average").asDouble() : 0.0);
                    movie.setReleaseDate(node.has("release_date") && !node.get("release_date").isNull() ? node.get("release_date").asText() : "");
                    movie.setPosterPath(node.has("poster_path") && !node.get("poster_path").isNull() ? node.get("poster_path").asText() : null);

                    movies.add(movie);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse TMDB response", e);
        }
        return movies;
    }
}