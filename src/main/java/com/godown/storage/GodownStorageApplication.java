package com.godown.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GodownStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(GodownStorageApplication.class, args);
    }
}