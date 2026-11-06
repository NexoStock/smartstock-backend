package com.nexostock.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point of the NexoStock backend.
 * Entrada principal del backend.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class NexostockBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                NexostockBackendApplication.class,
                args
        );
    }
}