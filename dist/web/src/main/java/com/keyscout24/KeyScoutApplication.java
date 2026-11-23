package com.keyscout24;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point. Replaces create_app() in app/__init__.py. */
@SpringBootApplication
public class KeyScoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeyScoutApplication.class, args);
    }
}
