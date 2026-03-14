package com.example.websecurity.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByIdAndUser(Long id, User user);
}
