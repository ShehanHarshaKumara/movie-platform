package com.movierental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MovieRentalApplication {
    public static void main(String[] args) {
        SpringApplication.run(MovieRentalApplication.class, args);
    }
}
