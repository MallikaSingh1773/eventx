package com.eventx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventXApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventXApplication.class, args);
    }
}
