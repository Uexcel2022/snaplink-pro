package com.uexcel.snaplinkpro;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class SnaplinkProApplication {
    private final Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(SnaplinkProApplication.class, args);
    }


    @PostConstruct
    public void logProfiles() {
        log.info(
                "Active Profiles={}",
                        Arrays.toString(environment.getActiveProfiles())
        );
    }
}

