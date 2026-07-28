package com.tumo.finalproject.controller;

import com.tumo.finalproject.model.Movie;
import com.tumo.finalproject.service.FavoritesService;
import com.tumo.finalproject.service.TmdbService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "*")
public class MovieController {

    private final TmdbService tmdbService;
    private final FavoritesService favoritesService;

    public MovieController(TmdbService tmdbService, FavoritesService favoritesService) {
        this.tmdbService = tmdbService;
        this.favoritesService = favoritesService;
    }

    private String currentUser(HttpSession session) {
        return (String) session.getAttribute("username");
    }

    /**
     * Search Movies via TMDB
     * Usage: GET http://localhost:8080/api/movies/search?query=Inception
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchMovies(@RequestParam(required = false) String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query parameter 'query' is required. Example: ?query=Inception"));
        }
        try {
            List<Movie> results = tmdbService.searchMovies(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search TMDB movies: " + e.getMessage()));
        }
    }

    /**
     * Get Favorites for logged-in user
     * Usage: GET http://localhost:8080/api/movies/favorites
     */
    @GetMapping("/favorites")
    public ResponseEntity<List<Movie>> getFavorites(HttpSession session) {
        String username = currentUser(session);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(favoritesService.getFavorites(username));
    }

    /**
     * Add Favorite for logged-in user
     * Usage: POST http://localhost:8080/api/movies/favorites
     */
    @PostMapping("/favorites")
    public ResponseEntity<?> addFavorite(@RequestBody Movie movie, HttpSession session) {
        String username = currentUser(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "You must be logged in to add favorites."));
        }
        if (movie == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Movie details are required."));
        }

        Movie savedMovie = favoritesService.addFavorite(username, movie);
        return ResponseEntity.ok(savedMovie);
    }

    /**
     * Remove Favorite by Movie ID
     * Usage: DELETE http://localhost:8080/api/movies/favorites/{id}
     */
    @DeleteMapping("/favorites/{id}")
    public ResponseEntity<Void> removeFavorite(@PathVariable int id, HttpSession session) {
        String username = currentUser(session);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        boolean removed = favoritesService.removeFavorite(username, id);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}