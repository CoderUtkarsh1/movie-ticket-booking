package com.moviebooking.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    private Long userId;
    private String userEmail;
    private String userPhone;
    private String type;      // EMAIL, SMS
    private String template;  // BOOKING_CONFIRMED, PAYMENT_SUCCESS, BOOKING_CANCELLED
    private String subject;
    private String message;
    private String bookingCode;
    private LocalDateTime timestamp;
}
