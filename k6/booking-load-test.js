/**
 * k6 Load Test — Movie Ticket Booking API
 * Run: k6 run k6/booking-load-test.js
 *
 * Prerequisites: All services running locally
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const bookingDuration = new Trend('booking_duration');

export const options = {
    stages: [
        { duration: '10s', target: 10 },   // Ramp up to 10 users
        { duration: '30s', target: 30 },   // Ramp up to 30 users
        { duration: '1m', target: 50 },    // Hold 50 concurrent users
        { duration: '10s', target: 0 },    // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],   // 95% requests under 500ms
        http_req_failed: ['rate<0.01'],      // Less than 1% failure rate
        errors: ['rate<0.05'],               // Custom error rate < 5%
    },
};

const BASE_URL = 'http://localhost:8080'; // API Gateway

// Login first to get JWT token
export function setup() {
    const loginRes = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
        email: 'test@example.com',
        password: 'password123',
    }), { headers: { 'Content-Type': 'application/json' } });

    return { token: loginRes.json('data.accessToken') || 'dummy-token' };
}

export default function (data) {
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${data.token}`,
    };

    // Scenario 1: Browse movies
    const moviesRes = http.get(`${BASE_URL}/api/movies`, { headers });
    check(moviesRes, {
        'movies: status 200': (r) => r.status === 200,
        'movies: has data': (r) => r.json('data') !== null,
    }) || errorRate.add(1);

    sleep(1);

    // Scenario 2: Get shows for a movie
    const showsRes = http.get(`${BASE_URL}/api/shows/movie/mov-001`, { headers });
    check(showsRes, {
        'shows: status 200': (r) => r.status === 200,
    }) || errorRate.add(1);

    sleep(0.5);

    // Scenario 3: Get available seats
    const seatsRes = http.get(`${BASE_URL}/api/shows/1/seats`, { headers });
    check(seatsRes, {
        'seats: status 200': (r) => r.status === 200,
    }) || errorRate.add(1);

    sleep(0.5);

    // Scenario 4: Health check
    const healthRes = http.get(`${BASE_URL}/actuator/health`);
    check(healthRes, {
        'health: status 200': (r) => r.status === 200,
    });

    sleep(1);
}
