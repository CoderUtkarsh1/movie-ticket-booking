package com.moviebooking.common.constants;

public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // Kafka Topics (only for Notification Service downstream)
    public static final String TOPIC_BOOKING_CONFIRMED = "booking-confirmed";
    public static final String TOPIC_BOOKING_CANCELLED = "booking-cancelled";
    public static final String TOPIC_NOTIFICATION = "notification";

    // Pagination Defaults
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIR = "asc";

    // Seat Lock TTL (seconds)
    public static final int SEAT_LOCK_TTL = 300;  // 5 minutes

    // Booking Code Prefix
    public static final String BOOKING_CODE_PREFIX = "MBK";

    // Roles
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_ADMIN = "ADMIN";

    // Headers (forwarded by API Gateway)
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLE = "X-User-Role";
}
