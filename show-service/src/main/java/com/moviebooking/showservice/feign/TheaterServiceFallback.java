package com.moviebooking.showservice.feign;

import com.moviebooking.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TheaterServiceFallback implements TheaterServiceClient {

    @Override
    public ApiResponse<List<Map<String, Object>>> getSeatsByScreenId(Long screenId) {
        log.error("[CIRCUIT BREAKER] theater-service is DOWN — getSeatsByScreenId fallback triggered for screenId={}", screenId);
        // Return empty list — show will be created without auto-populated seats.
        // Admin can retry creating seats later once theater-service is back.
        return ApiResponse.success("Fallback: theater-service unavailable, no seats populated", Collections.emptyList());
    }
}
