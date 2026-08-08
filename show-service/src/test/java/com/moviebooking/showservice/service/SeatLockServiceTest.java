package com.moviebooking.showservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for SeatLockService (Redis-based seat locking)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeatLockService Unit Tests")
class SeatLockServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private SeatLockService seatLockService;

    @BeforeEach
    void setUp() {
        seatLockService = new SeatLockService(redisTemplate);
        ReflectionTestUtils.setField(seatLockService, "seatLockTtl", 300);
    }

    @Nested
    @DisplayName("lockSeats()")
    class LockSeats {

        @Test
        @DisplayName("should return true when all seats locked successfully via Lua script")
        void shouldReturnTrueOnSuccess() {
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString()))
                    .thenReturn(1L); // Lua returns 1 = success

            boolean result = seatLockService.lockSeats(1L, List.of(101L, 102L), 1L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when another user holds a lock")
        void shouldReturnFalseOnConflict() {
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString()))
                    .thenReturn(0L); // Lua returns 0 = conflict

            boolean result = seatLockService.lockSeats(1L, List.of(101L), 1L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("releaseSeats()")
    class ReleaseSeats {

        @Test
        @DisplayName("should execute Lua unlock script for given seat keys")
        void shouldExecuteUnlockScript() {
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList()))
                    .thenReturn(2L); // 2 keys deleted

            seatLockService.releaseSeats(1L, List.of(101L, 102L));

            verify(redisTemplate).execute(any(DefaultRedisScript.class), anyList());
        }
    }

    @Nested
    @DisplayName("isSeatLocked()")
    class IsSeatLocked {

        @Test
        @DisplayName("should return true when Redis key exists")
        void shouldReturnTrueWhenLocked() {
            when(redisTemplate.hasKey("seat:lock:1:101")).thenReturn(true);

            boolean result = seatLockService.isSeatLocked(1L, 101L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when Redis key does not exist")
        void shouldReturnFalseWhenNotLocked() {
            when(redisTemplate.hasKey("seat:lock:1:101")).thenReturn(false);

            boolean result = seatLockService.isSeatLocked(1L, 101L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getLockedByUserId()")
    class GetLockedByUserId {

        @Test
        @DisplayName("should return userId from Redis value")
        void shouldReturnUserId() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("seat:lock:1:101")).thenReturn("42");

            Long result = seatLockService.getLockedByUserId(1L, 101L);

            assertThat(result).isEqualTo(42L);
        }

        @Test
        @DisplayName("should return null when seat is not locked")
        void shouldReturnNullWhenNotLocked() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("seat:lock:1:101")).thenReturn(null);

            Long result = seatLockService.getLockedByUserId(1L, 101L);

            assertThat(result).isNull();
        }
    }
}
