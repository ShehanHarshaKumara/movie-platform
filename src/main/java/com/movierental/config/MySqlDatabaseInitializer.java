package com.movierental.config;

import com.movierental.model.Movie;
import com.movierental.model.Rental;
import com.movierental.model.Review;
import com.movierental.model.User;
import com.movierental.repository.FileHandler;
import com.movierental.service.MovieService;
import com.movierental.service.RentalService;
import com.movierental.service.ReviewService;
import com.movierental.service.UserService;
import com.movierental.util.StorageCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("mysql")
@Order(2)
public class MySqlDatabaseInitializer implements CommandLineRunner {

    private static final String RECENTLY_WATCHED_FILE = "recently-watched.txt";

    private final JdbcTemplate jdbcTemplate;
    private final FileHandler fileHandler;
    private final UserService userService;
    private final MovieService movieService;
    private final RentalService rentalService;
    private final ReviewService reviewService;
    private final boolean seedFromFiles;

    public MySqlDatabaseInitializer(JdbcTemplate jdbcTemplate,
                                    FileHandler fileHandler,
                                    UserService userService,
                                    MovieService movieService,
                                    RentalService rentalService,
                                    ReviewService reviewService,
                                    @Value("${app.mysql.seed-from-files:true}") boolean seedFromFiles) {
        this.jdbcTemplate = jdbcTemplate;
        this.fileHandler = fileHandler;
        this.userService = userService;
        this.movieService = movieService;
        this.rentalService = rentalService;
        this.reviewService = reviewService;
        this.seedFromFiles = seedFromFiles;
    }

    @Override
    public void run(String... args) {
        createTables();
        if (seedFromFiles) {
            seedUsers();
            seedMovies();
            seedRentals();
            seedReviews();
            seedRecentlyWatched();
        }
    }

    private void createTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    full_name VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    registration_date DATETIME NOT NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS movies (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(200) NOT NULL,
                    director VARCHAR(150) NOT NULL,
                    genre VARCHAR(100) NOT NULL,
                    release_date DATE NOT NULL,
                    rental_price DECIMAL(10,2) NOT NULL,
                    available_copies INT NOT NULL,
                    total_copies INT NOT NULL,
                    description TEXT
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS rentals (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    movie_id INT NOT NULL,
                    rental_date DATETIME NOT NULL,
                    return_date DATETIME NULL,
                    due_date DATETIME NOT NULL,
                    rental_fee DECIMAL(10,2) NOT NULL,
                    returned BOOLEAN NOT NULL DEFAULT FALSE,
                    late_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                    CONSTRAINT fk_rentals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    CONSTRAINT fk_rentals_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS reviews (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    movie_id INT NOT NULL,
                    username VARCHAR(50) NOT NULL,
                    movie_title VARCHAR(200) NOT NULL,
                    rating INT NOT NULL,
                    comment TEXT,
                    review_date DATETIME NOT NULL,
                    approved BOOLEAN NOT NULL DEFAULT TRUE,
                    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    CONSTRAINT fk_reviews_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS recently_watched (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    movie_id INT NOT NULL,
                    watched_at DATETIME NOT NULL,
                    CONSTRAINT fk_recent_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    CONSTRAINT fk_recent_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
                )
                """);
    }

    private void seedUsers() {
        if (tableCount("users") > 0) {
            return;
        }

        for (User user : userService.getAllUsers()) {
            jdbcTemplate.update(
                    """
                            INSERT INTO users
                            (id, username, password, email, full_name, role, registration_date, active)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole(),
                    Timestamp.valueOf(user.getRegistrationDate()),
                    user.isActive()
            );
        }
    }

    private void seedMovies() {
        if (tableCount("movies") > 0) {
            return;
        }

        for (Movie movie : movieService.getAllMovies()) {
            jdbcTemplate.update(
                    """
                            INSERT INTO movies
                            (id, title, director, genre, release_date, rental_price, available_copies, total_copies, description)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    movie.getId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getGenre(),
                    Date.valueOf(movie.getReleaseDate()),
                    movie.getRentalPrice(),
                    movie.getAvailableCopies(),
                    movie.getTotalCopies(),
                    movie.getDescription()
            );
        }
    }

    private void seedRentals() {
        if (tableCount("rentals") > 0) {
            return;
        }

        for (Rental rental : rentalService.getAllRentals()) {
            jdbcTemplate.update(
                    """
                            INSERT INTO rentals
                            (id, user_id, movie_id, rental_date, return_date, due_date, rental_fee, returned, late_fee)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    rental.getId(),
                    rental.getUserId(),
                    rental.getMovieId(),
                    Timestamp.valueOf(rental.getRentalDate()),
                    rental.getReturnDate() == null ? null : Timestamp.valueOf(rental.getReturnDate()),
                    Timestamp.valueOf(rental.getDueDate()),
                    rental.getRentalFee(),
                    rental.isReturned(),
                    rental.getLateFee()
            );
        }
    }

    private void seedReviews() {
        if (tableCount("reviews") > 0) {
            return;
        }

        for (Review review : reviewService.getAllReviews()) {
            jdbcTemplate.update(
                    """
                            INSERT INTO reviews
                            (id, user_id, movie_id, username, movie_title, rating, comment, review_date, approved)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    review.getId(),
                    review.getUserId(),
                    review.getMovieId(),
                    review.getUsername(),
                    review.getMovieTitle(),
                    review.getRating(),
                    review.getComment(),
                    Timestamp.valueOf(review.getReviewDate()),
                    review.isApproved()
            );
        }
    }

    private void seedRecentlyWatched() {
        if (tableCount("recently_watched") > 0) {
            return;
        }

        for (String line : fileHandler.readFromFile(RECENTLY_WATCHED_FILE)) {
            List<String> fields = StorageCodec.parseCsv(line);
            if (fields.size() < 3) {
                continue;
            }

            int userId = Integer.parseInt(fields.get(0));
            int movieId = Integer.parseInt(fields.get(1));
            LocalDateTime watchedAt = LocalDateTime.parse(fields.get(2));

            jdbcTemplate.update(
                    "INSERT INTO recently_watched (user_id, movie_id, watched_at) VALUES (?, ?, ?)",
                    userId,
                    movieId,
                    Timestamp.valueOf(watchedAt)
            );
        }
    }

    private int tableCount(String tableName) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
        return count == null ? 0 : count;
    }
}
