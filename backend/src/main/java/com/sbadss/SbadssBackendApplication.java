package com.sbadss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SbadssBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SbadssBackendApplication.class, args);
    }
}
