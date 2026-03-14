package com.example.websecurity.service;

import com.example.websecurity.exception.AccessDeniedException;
import com.example.websecurity.persistence.Movie;
import com.example.websecurity.persistence.MovieRepository;
import com.example.websecurity.persistence.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static lombok.AccessLevel.PACKAGE;

@Service
@AllArgsConstructor(access = PACKAGE)
@Slf4j
public class MovieService {
    private final MovieRepository movieRepository;

    public Movie getMovieById(Long id, User user) {
        log.info("Movie Service: Getting movie from database by id: {}", id);
        Movie movie = movieRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AccessDeniedException("You do not have access to this movie"));
        log.info("Movie Service: Found movie with title: {}", movie.getTitle());
        return movie;
    }
}
