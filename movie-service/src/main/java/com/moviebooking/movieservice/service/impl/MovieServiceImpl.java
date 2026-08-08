package com.moviebooking.movieservice.service.impl;

import com.moviebooking.common.dto.PagedResponse;
import com.moviebooking.common.enums.MovieStatus;
import com.moviebooking.common.exception.ResourceNotFoundException;
import com.moviebooking.movieservice.dto.MovieRequest;
import com.moviebooking.movieservice.dto.ReviewRequest;
import com.moviebooking.movieservice.entity.Movie;
import com.moviebooking.movieservice.entity.Review;
import com.moviebooking.movieservice.repository.MovieRepository;
import com.moviebooking.movieservice.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public Movie createMovie(MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .duration(request.getDuration())
                .language(request.getLanguage())
                .genre(request.getGenre())
                .releaseDate(request.getReleaseDate())
                .posterUrl(request.getPosterUrl())
                .trailerUrl(request.getTrailerUrl())
                .status(request.getStatus() != null ? MovieStatus.valueOf(request.getStatus()) : MovieStatus.UPCOMING)
                .imdbRating(request.getImdbRating())
                .cast(request.getCast())
                .build();

        movie = movieRepository.save(movie);
        log.info("Movie created: {}", movie.getTitle());
        return movie;
    }

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public Movie updateMovie(String id, MovieRequest request) {
        Movie movie = getMovieById(id);

        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getDescription() != null) movie.setDescription(request.getDescription());
        if (request.getDuration() != null) movie.setDuration(request.getDuration());
        if (request.getLanguage() != null) movie.setLanguage(request.getLanguage());
        if (request.getGenre() != null) movie.setGenre(request.getGenre());
        if (request.getReleaseDate() != null) movie.setReleaseDate(request.getReleaseDate());
        if (request.getPosterUrl() != null) movie.setPosterUrl(request.getPosterUrl());
        if (request.getTrailerUrl() != null) movie.setTrailerUrl(request.getTrailerUrl());
        if (request.getStatus() != null) movie.setStatus(MovieStatus.valueOf(request.getStatus()));
        if (request.getImdbRating() != null) movie.setImdbRating(request.getImdbRating());
        if (request.getCast() != null) movie.setCast(request.getCast());

        movie = movieRepository.save(movie);
        log.info("Movie updated: {}", movie.getTitle());
        return movie;
    }

    @Override
    public Movie getMovieById(String id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
    }

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public void deleteMovie(String id) {
        Movie movie = getMovieById(id);
        movieRepository.delete(movie);
        log.info("Movie deleted: {}", movie.getTitle());
    }

    @Override
    @Cacheable(value = "movies", key = "#genre + '-' + #language + '-' + #status + '-' + #page + '-' + #size")
    public PagedResponse<Movie> getAllMovies(String genre, String language, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending());
        Page<Movie> moviePage;

        if (status != null && genre != null) {
            moviePage = movieRepository.findByStatusAndGenre(MovieStatus.valueOf(status), genre, pageable);
        } else if (status != null && language != null) {
            moviePage = movieRepository.findByStatusAndLanguage(MovieStatus.valueOf(status), language, pageable);
        } else if (status != null) {
            moviePage = movieRepository.findByStatus(MovieStatus.valueOf(status), pageable);
        } else if (genre != null) {
            moviePage = movieRepository.findByGenre(genre, pageable);
        } else if (language != null) {
            moviePage = movieRepository.findByLanguage(language, pageable);
        } else {
            moviePage = movieRepository.findAll(pageable);
        }

        return buildPagedResponse(moviePage);
    }

    @Override
    public PagedResponse<Movie> searchMovies(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> moviePage = movieRepository.searchMovies(keyword, pageable);
        return buildPagedResponse(moviePage);
    }

    @Override
    @Cacheable(value = "movies", key = "'now-showing-' + #page + '-' + #size")
    public PagedResponse<Movie> getNowShowingMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending());
        Page<Movie> moviePage = movieRepository.findByStatus(MovieStatus.NOW_SHOWING, pageable);
        return buildPagedResponse(moviePage);
    }

    @Override
    @Cacheable(value = "movies", key = "'upcoming-' + #page + '-' + #size")
    public PagedResponse<Movie> getUpcomingMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").ascending());
        Page<Movie> moviePage = movieRepository.findByStatus(MovieStatus.UPCOMING, pageable);
        return buildPagedResponse(moviePage);
    }

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public Movie addReview(String movieId, Long userId, String userName, ReviewRequest request) {
        Movie movie = getMovieById(movieId);

        Review review = Review.builder()
                .userId(userId)
                .userName(userName)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        movie.getReviews().add(review);

        // Recalculate average rating
        double avgRating = movie.getReviews().stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        movie.setRating(Math.round(avgRating * 10.0) / 10.0);

        movie = movieRepository.save(movie);
        log.info("Review added to movie: {} by user: {}", movieId, userId);
        return movie;
    }

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public Movie toggleInterested(String movieId, Long userId) {
        Movie movie = getMovieById(movieId);
        
        if (movie.getInterestedUserIds().contains(userId)) {
            movie.getInterestedUserIds().remove(userId);
        } else {
            movie.getInterestedUserIds().add(userId);
        }
        
        movie = movieRepository.save(movie);
        log.info("User {} toggled interest for movie {}", userId, movieId);
        return movie;
    }

    private PagedResponse<Movie> buildPagedResponse(Page<Movie> page) {
        return PagedResponse.<Movie>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
