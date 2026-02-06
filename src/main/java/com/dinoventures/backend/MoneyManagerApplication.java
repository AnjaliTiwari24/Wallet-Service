package com.dinoventures.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.dinoventures.backend")
@Slf4j
public class MoneyManagerApplication {

    public static void main(String[] args) {
        log.info("Starting Money Manager Backend Application...");
        SpringApplication.run(MoneyManagerApplication.class, args);
        log.info("Money Manager Backend Application started successfully");
    }
}
