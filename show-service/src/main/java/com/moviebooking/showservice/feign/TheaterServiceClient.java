package com.moviebooking.showservice.feign;

import com.moviebooking.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "theater-service", fallback = TheaterServiceFallback.class)
public interface TheaterServiceClient {

    @GetMapping("/api/screens/{id}/seats")
    ApiResponse<List<Map<String, Object>>> getSeatsByScreenId(@PathVariable("id") Long screenId);
}
