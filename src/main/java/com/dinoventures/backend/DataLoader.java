package com.dinoventures.backend;

import com.dinoventures.backend.dto.RegisterRequest;
import com.dinoventures.backend.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final AuthService authService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading sample data...");

        try {
            // Create sample user for testing
            RegisterRequest testUser = RegisterRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .password("Test@1234")
                    .confirmPassword("Test@1234")
                    .build();

            authService.register(testUser);
            log.info("Sample user created successfully");
        } catch (Exception e) {
            log.info("Sample user already exists or error occurred: {}", e.getMessage());
        }
    }
}
