package com.moviebooking.notificationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;

    @Indexed
    private Long userId;

    private String userEmail;

    private String type;      // BOOKING_CONFIRMED, PAYMENT_SUCCESS, BOOKING_CANCELLED

    private String subject;

    private String message;

    private String bookingCode;

    @Builder.Default
    private boolean sent = false;

    private String channel;   // EMAIL, SMS

    @Builder.Default
    private boolean read = false;

    @CreatedDate
    private LocalDateTime createdAt;
}
