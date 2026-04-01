CREATE DATABASE IF NOT EXISTS movie_rental_platform_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE movie_rental_platform_db;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    registration_date DATETIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

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
);

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
);

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
);

CREATE TABLE IF NOT EXISTS recently_watched (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    watched_at DATETIME NOT NULL,
    CONSTRAINT fk_recent_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recent_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
);
