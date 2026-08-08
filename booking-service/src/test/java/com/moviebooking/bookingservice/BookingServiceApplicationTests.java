package com.moviebooking.bookingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Context load test — verifies Spring Boot application starts without errors.
 * Uses @ActiveProfiles("test") to disable Eureka, Config Server, Kafka.
 * NOTE: This test is DISABLED because it requires full infrastructure (DB, Kafka).
 *       Unit tests and slice tests cover the same functionality without full context.
 */
@SpringBootTest
@ActiveProfiles("test")
@org.junit.jupiter.api.Disabled("Requires full infrastructure — use unit/slice tests instead")
class BookingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
