package com.moviebooking.apigateway.config;

import com.moviebooking.apigateway.filter.JwtAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ===== PUBLIC ROUTES (No JWT required) =====

                // Auth endpoints - Login & Register
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))

                // Movie listing - Public access (specific endpoints)
                .route("movie-public", r -> r
                        .path("/api/movies/now-showing", "/api/movies/upcoming", "/api/movies/search")
                        .uri("lb://movie-service"))

                // Movie details - Public GET access
                .route("movie-details-public", r -> r
                        .method("GET")
                        .and()
                        .path("/api/movies/{id}")
                        .uri("lb://movie-service"))

                // All movies list - Public GET (for admin dropdown + home page)
                .route("movie-list-public", r -> r
                        .method("GET")
                        .and()
                        .path("/api/movies")
                        .uri("lb://movie-service"))

                // Theater listing - Public GET access
                .route("theater-public", r -> r
                        .method("GET")
                        .and()
                        .path("/api/theaters/**")
                        .uri("lb://theater-service"))

                // Show listing - Public GET access
                .route("show-public", r -> r
                        .method("GET")
                        .and()
                        .path("/api/shows/**")
                        .uri("lb://show-service"))

                // ===== PROTECTED ROUTES (JWT required) =====

                // User profile endpoints
                .route("user-service-protected", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://user-service"))

                // Movie admin endpoints (POST, PUT, DELETE only — GET is public)
                .route("movie-admin", r -> r
                        .method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .and()
                        .path("/api/movies/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://movie-service"))

                // Theater admin endpoints (POST, PUT, DELETE — GET is public)
                .route("theater-admin", r -> r
                        .method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .and()
                        .path("/api/theaters/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://theater-service"))

                // Show admin endpoints (POST, PUT, DELETE — GET is public)
                .route("show-admin", r -> r
                        .method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .and()
                        .path("/api/shows/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://show-service"))

                // Booking endpoints
                .route("booking-service-protected", r -> r
                        .path("/api/bookings/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://booking-service"))

                // Payment endpoints
                .route("payment-service-protected", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://payment-service"))

                // Screen/Seat admin endpoints
                .route("screen-admin", r -> r
                        .path("/api/screens/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://theater-service"))

                // Notification endpoints
                .route("notification-service-protected", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://notification-service"))

                .build();
    }
}
