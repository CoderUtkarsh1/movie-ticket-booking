package com.moviebooking.userservice.service;

import com.moviebooking.userservice.dto.UpdateProfileRequest;
import com.moviebooking.userservice.dto.UserResponse;

public interface UserService {

    UserResponse getUserProfile(Long userId);

    UserResponse getUserById(Long id);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
}
