package com.movierental.config;

import com.movierental.service.MovieService;
import com.movierental.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final MovieService movieService;

    public DataInitializer(UserService userService, MovieService movieService) {
        this.userService = userService;
        this.movieService = movieService;
    }

    @Override
    public void run(String... args) {
        userService.ensureAdminUser();
        movieService.ensureSampleMovies();
    }
}
