package com.moviebooking.userservice.controller;

import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.userservice.dto.*;
import com.moviebooking.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return buildResponseWithCookies(response, HttpStatus.CREATED, "User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return buildResponseWithCookies(response, HttpStatus.OK, "Login successful");
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        AuthResponse response = authService.refreshToken(refreshToken);
        return buildResponseWithCookies(response, HttpStatus.OK, "Token refreshed successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Clear cookies by setting maxAge to 0
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)  // Set to true in production with HTTPS
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("Logged out successfully", null));
    }

    /**
     * Builds response with httpOnly cookies for JWT tokens.
     * Tokens are sent both in response body (for initial setup) and as httpOnly cookies (for XSS safety).
     */
    private ResponseEntity<ApiResponse<AuthResponse>> buildResponseWithCookies(
            AuthResponse response, HttpStatus status, String message) {

        // httpOnly cookie for access token (24 hours)
        ResponseCookie accessCookie = ResponseCookie.from("access_token", response.getAccessToken())
                .httpOnly(true)
                .secure(false)  // Set to true in production with HTTPS
                .path("/")
                .maxAge(24 * 60 * 60)  // 24 hours
                .sameSite("Lax")
                .build();

        // httpOnly cookie for refresh token (7 days)
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", response.getRefreshToken())
                .httpOnly(true)
                .secure(false)  // Set to true in production with HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60)  // 7 days
                .sameSite("Lax")
                .build();

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success(message, response));
    }
}
