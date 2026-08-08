package com.moviebooking.bookingservice.kafka;

import com.moviebooking.common.constants.AppConstants;
import com.moviebooking.common.event.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventProducer {

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    public void sendBookingConfirmed(BookingEvent event) {
        kafkaTemplate.send(AppConstants.TOPIC_BOOKING_CONFIRMED, event.getBookingCode(), event);
        log.info("Kafka: BOOKING_CONFIRMED event sent for {}", event.getBookingCode());
    }

    public void sendBookingCancelled(BookingEvent event) {
        kafkaTemplate.send(AppConstants.TOPIC_BOOKING_CANCELLED, event.getBookingCode(), event);
        log.info("Kafka: BOOKING_CANCELLED event sent for {}", event.getBookingCode());
    }
}
