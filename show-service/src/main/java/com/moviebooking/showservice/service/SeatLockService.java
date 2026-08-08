package com.moviebooking.showservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis-based seat locking service.
 * When a user selects seats, they are "blocked" in Redis with a 5-minute TTL.
 * If payment is not completed within 5 minutes, seats are auto-released.
 *
 * Uses Lua scripts for atomic batch lock/unlock operations to prevent race conditions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeatLockService {

    private final StringRedisTemplate redisTemplate;

    @Value("${seat.lock.ttl:300}")
    private int seatLockTtl;

    private static final String SEAT_LOCK_PREFIX = "seat:lock:";

    /**
     * Lua script for atomic batch lock.
     * Checks all seats first, then locks all or none (atomic).
     * Returns:
     *   1 = all seats locked successfully
     *   0 = at least one seat is locked by another user (no seats locked)
     *  -1 = re-lock scenario (same user, TTL extended)
     */
    private static final String LOCK_SEATS_LUA =
            "local ttl = tonumber(ARGV[1])\n" +
            "local userId = ARGV[2]\n" +
            "local keyCount = #KEYS\n" +
            "\n" +
            "-- Phase 1: Check all seats\n" +
            "for i = 1, keyCount do\n" +
            "    local existing = redis.call('GET', KEYS[i])\n" +
            "    if existing and existing ~= userId then\n" +
            "        return 0  -- Another user holds a lock\n" +
            "    end\n" +
            "end\n" +
            "\n" +
            "-- Phase 2: Lock all seats (all-or-nothing)\n" +
            "for i = 1, keyCount do\n" +
            "    redis.call('SET', KEYS[i], userId, 'EX', ttl)\n" +
            "end\n" +
            "\n" +
            "return 1";

    /**
     * Lua script for atomic batch unlock.
     * Only deletes keys that belong to the specified user (or any user if userId is '*').
     */
    private static final String UNLOCK_SEATS_LUA =
            "local deleted = 0\n" +
            "for i = 1, #KEYS do\n" +
            "    redis.call('DEL', KEYS[i])\n" +
            "    deleted = deleted + 1\n" +
            "end\n" +
            "return deleted";

    /**
     * Lock seats atomically for a user using Lua script.
     * Either ALL seats are locked or NONE are locked.
     */
    public boolean lockSeats(Long showId, List<Long> seatIds, Long userId) {
        List<String> keys = seatIds.stream()
                .map(seatId -> buildKey(showId, seatId))
                .collect(Collectors.toList());

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LOCK_SEATS_LUA, Long.class);

        Long result = redisTemplate.execute(script, keys,
                String.valueOf(seatLockTtl), String.valueOf(userId));

        if (result != null && result == 1L) {
            log.info("Seats locked atomically for show {} by user {} (TTL: {}s): {}",
                    showId, userId, seatLockTtl, seatIds);
            return true;
        } else {
            log.warn("Failed to lock seats for show {} by user {}: another user holds a lock. Seats: {}",
                    showId, userId, seatIds);
            return false;
        }
    }

    /**
     * Release seats atomically (unlock from Redis)
     */
    public void releaseSeats(Long showId, List<Long> seatIds) {
        List<String> keys = seatIds.stream()
                .map(seatId -> buildKey(showId, seatId))
                .collect(Collectors.toList());

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SEATS_LUA, Long.class);

        Long deleted = redisTemplate.execute(script, keys);
        log.info("Seats released atomically for show {}: {} (deleted {} keys)", showId, seatIds, deleted);
    }

    /**
     * Check if a seat is locked
     */
    public boolean isSeatLocked(Long showId, Long seatId) {
        String key = buildKey(showId, seatId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Get the user who locked a seat
     */
    public Long getLockedByUserId(Long showId, Long seatId) {
        String key = buildKey(showId, seatId);
        String userId = redisTemplate.opsForValue().get(key);
        return userId != null ? Long.parseLong(userId) : null;
    }

    private String buildKey(Long showId, Long seatId) {
        return SEAT_LOCK_PREFIX + showId + ":" + seatId;
    }
}
