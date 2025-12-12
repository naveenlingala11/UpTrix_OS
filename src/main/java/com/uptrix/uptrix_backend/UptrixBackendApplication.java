package com.uptrix.uptrix_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UptrixBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UptrixBackendApplication.class, args);
    }

}
