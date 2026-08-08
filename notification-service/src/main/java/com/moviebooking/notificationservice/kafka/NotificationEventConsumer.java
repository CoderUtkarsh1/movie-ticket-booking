package com.moviebooking.notificationservice.kafka;

import com.moviebooking.common.constants.AppConstants;
import com.moviebooking.common.event.BookingEvent;
import com.moviebooking.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = AppConstants.TOPIC_BOOKING_CONFIRMED, groupId = "notification-group")
    public void handleBookingConfirmed(BookingEvent event) {
        log.info("Kafka: BOOKING_CONFIRMED received for {}", event.getBookingCode());

        String showDetails = event.getShowDate() + " at " + event.getShowTime();

        emailService.sendBookingConfirmation(
                event.getUserId(),
                event.getUserEmail(),
                event.getBookingCode(),
                event.getMovieName(),
                event.getTheaterName(),
                showDetails
        );
    }

    // PAYMENT_SUCCESS listener removed — payment-service no longer publishes to Kafka.
    // Payment confirmation is now handled synchronously via Feign calls.

    @KafkaListener(topics = AppConstants.TOPIC_BOOKING_CANCELLED, groupId = "notification-group")
    public void handleBookingCancelled(BookingEvent event) {
        log.info("Kafka: BOOKING_CANCELLED received for {}", event.getBookingCode());

        emailService.sendBookingCancellation(
                event.getUserId(),
                event.getUserEmail(),
                event.getBookingCode()
        );
    }
}
