package com.tumo.finalproject.service;

import com.tumo.finalproject.model.FavoriteMovie;
import com.tumo.finalproject.model.Movie;
import com.tumo.finalproject.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoritesService {

    private final FavoriteRepository favoriteRepository;

    public FavoritesService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public List<Movie> getFavorites(String username) {
        return favoriteRepository.findByUsername(username).stream()
                .map(this::toMovie)
                .toList();
    }

    public Movie addFavorite(String username, Movie movie) {
        if (movie == null) {
            return null;
        }
        if (!favoriteRepository.existsByUsernameAndTmdbId(username, movie.getId())) {
            favoriteRepository.save(toEntity(username, movie));
        }
        return movie;
    }

    @Transactional
    public boolean removeFavorite(String username, int tmdbId) {
        return favoriteRepository.deleteByUsernameAndTmdbId(username, tmdbId) > 0;
    }

    private Movie toMovie(FavoriteMovie f) {
        return new Movie(
                f.getTmdbId(),
                f.getTitle(),
                f.getOverview(),
                f.getVoteAverage(),
                f.getReleaseDate(),
                f.getPosterPath()
        );
    }

    private FavoriteMovie toEntity(String username, Movie m) {
        String overview = m.getOverview();
        // Truncate overview if it exceeds 2000 characters to prevent database errors
        if (overview != null && overview.length() > 2000) {
            overview = overview.substring(0, 1997) + "...";
        }

        return new FavoriteMovie(
                username,
                m.getId(),
                m.getTitle(),
                overview,
                m.getVoteAverage(),
                m.getReleaseDate(),
                m.getPosterPath()
        );
    }
}