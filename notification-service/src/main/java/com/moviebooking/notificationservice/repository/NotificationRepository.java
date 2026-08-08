package com.moviebooking.notificationservice.repository;

import com.moviebooking.notificationservice.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByBookingCode(String bookingCode);
    long countByUserIdAndReadFalse(Long userId);
}
