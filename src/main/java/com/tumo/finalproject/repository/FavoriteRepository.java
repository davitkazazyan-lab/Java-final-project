package com.tumo.finalproject.repository;

import com.tumo.finalproject.model.FavoriteMovie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<FavoriteMovie, Long> {

    List<FavoriteMovie> findByUsername(String username);

    boolean existsByUsernameAndTmdbId(String username, int tmdbId);

    long deleteByUsernameAndTmdbId(String username, int tmdbId);
}
