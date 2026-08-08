package com.moviebooking.notificationservice.service;

import com.moviebooking.notificationservice.entity.Notification;
import com.moviebooking.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    public void sendBookingConfirmation(Long userId, String toEmail, String bookingCode,
                                         String movieName, String theaterName, String showDetails) {
        String subject = "Booking Confirmed - " + bookingCode;
        String message = String.format(
                "Dear Customer,\n\n" +
                "Your booking has been CONFIRMED!\n\n" +
                "Booking Code: %s\n" +
                "Movie: %s\n" +
                "Theater: %s\n" +
                "Show: %s\n\n" +
                "Please arrive 15 minutes before showtime.\n\n" +
                "Thank you for choosing MovieBooking!\n",
                bookingCode, movieName, theaterName, showDetails
        );

        sendEmail(toEmail, subject, message);
        saveNotification(userId, toEmail, "BOOKING_CONFIRMED", subject, message, bookingCode);
    }

    public void sendPaymentConfirmation(Long userId, String toEmail, String bookingCode,
                                         String transactionId, String amount) {
        String subject = "Payment Successful - " + bookingCode;
        String message = String.format(
                "Dear Customer,\n\n" +
                "Your payment has been processed successfully!\n\n" +
                "Booking Code: %s\n" +
                "Transaction ID: %s\n" +
                "Amount: Rs. %s\n\n" +
                "Thank you!\n",
                bookingCode, transactionId, amount
        );

        sendEmail(toEmail, subject, message);
        saveNotification(userId, toEmail, "PAYMENT_SUCCESS", subject, message, bookingCode);
    }

    public void sendBookingCancellation(Long userId, String toEmail, String bookingCode) {
        String subject = "Booking Cancelled - " + bookingCode;
        String message = String.format(
                "Dear Customer,\n\n" +
                "Your booking %s has been cancelled.\n" +
                "Refund will be processed within 3-5 business days.\n\n" +
                "Thank you!\n",
                bookingCode
        );

        sendEmail(toEmail, subject, message);
        saveNotification(userId, toEmail, "BOOKING_CANCELLED", subject, message, bookingCode);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(text);
            mailSender.send(mailMessage);
            log.info("Email sent to: {} | Subject: {}", to, subject);
        } catch (Exception e) {
            // Don't fail the entire flow if email fails
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private void saveNotification(Long userId, String email, String type,
                                   String subject, String message, String bookingCode) {
        Notification notification = Notification.builder()
                .userId(userId)
                .userEmail(email)
                .type(type)
                .subject(subject)
                .message(message)
                .bookingCode(bookingCode)
                .sent(true)
                .channel("EMAIL")
                .build();

        notificationRepository.save(notification);
    }
}
