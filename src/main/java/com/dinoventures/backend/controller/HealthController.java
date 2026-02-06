package com.dinoventures.backend.controller;

import com.dinoventures.backend.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<String>> health() {
        log.info("Health check request received");
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Application is running successfully",
                "HEALTHY"
        ));
    }
}
