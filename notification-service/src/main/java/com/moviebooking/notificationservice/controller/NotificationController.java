package com.moviebooking.notificationservice.controller;

import com.moviebooking.common.constants.AppConstants;
import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.notificationservice.entity.Notification;
import com.moviebooking.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications(
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/booking/{bookingCode}")
    public ResponseEntity<ApiResponse<List<Notification>>> getByBookingCode(@PathVariable String bookingCode) {
        List<Notification> notifications = notificationRepository.findByBookingCode(bookingCode);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable String id) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                    return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId) {
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
