package com.moviebooking.movieservice.repository;

import com.moviebooking.common.enums.MovieStatus;
import com.moviebooking.movieservice.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {

    Page<Movie> findByStatus(MovieStatus status, Pageable pageable);

    Page<Movie> findByGenre(String genre, Pageable pageable);

    Page<Movie> findByLanguage(String language, Pageable pageable);

    Page<Movie> findByStatusAndGenre(MovieStatus status, String genre, Pageable pageable);

    Page<Movie> findByStatusAndLanguage(MovieStatus status, String language, Pageable pageable);

    @Query("{'$or': [{'title': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}")
    Page<Movie> searchMovies(String keyword, Pageable pageable);

    List<Movie> findByStatus(MovieStatus status);
}
