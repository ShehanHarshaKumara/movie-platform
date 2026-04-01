package com.movierental.service;

import com.movierental.model.Movie;
import com.movierental.model.Rental;
import com.movierental.repository.FileHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalService {

    private static final String RENTALS_FILE = "rentals.txt";
    private static final double LATE_FEE_PER_DAY = 2.0;

    private final FileHandler fileHandler;
    private final MovieService movieService;

    public RentalService(FileHandler fileHandler, MovieService movieService) {
        this.fileHandler = fileHandler;
        this.movieService = movieService;
    }

    public String validateRentalRequest(int userId, int movieId, int days) {
        if (days <= 0) {
            return "Rental days must be at least 1.";
        }

        Movie movie = movieService.getMovieById(movieId);
        if (movie == null) {
            return "Movie not found.";
        }
        if (!movieService.isAvailable(movieId)) {
            return "This movie is currently unavailable.";
        }
        if (getActiveRentalByUserAndMovie(userId, movieId) != null) {
            return "You already rented this movie.";
        }

        return "success";
    }

    public String rentMovie(int userId, int movieId, int days) {
        String validation = validateRentalRequest(userId, movieId, days);
        if (!"success".equals(validation)) {
            return validation;
        }

        Movie movie = movieService.getMovieById(movieId);

        Rental rental = new Rental();
        rental.setId(fileHandler.getNextId(RENTALS_FILE));
        rental.setUserId(userId);
        rental.setMovieId(movieId);
        rental.setRentalDate(LocalDateTime.now());
        rental.setDueDate(LocalDateTime.now().plusDays(days));
        rental.setRentalFee(movie.getRentalPrice() * days);

        fileHandler.writeToFile(RENTALS_FILE, List.of(rental), true);
        movieService.updateAvailableCopies(movieId, -1);
        return "success";
    }

    public boolean canUserDownloadMovie(int userId, int movieId) {
        Movie movie = movieService.getMovieById(movieId);
        if (movie == null || movie.getDownloadLink() == null || movie.getDownloadLink().isBlank()) {
            return false;
        }
        return getActiveRentalByUserAndMovie(userId, movieId) != null;
    }

    public double calculateRentalFee(int movieId, int days) {
        Movie movie = movieService.getMovieById(movieId);
        if (movie == null || days <= 0) {
            return 0.0;
        }
        return movie.getRentalPrice() * days;
    }

    public Rental getRentalById(int id) {
        return getAllRentals().stream()
                .filter(rental -> rental.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Rental> getRentalsByUser(int userId) {
        return getAllRentals().stream()
                .filter(rental -> rental.getUserId() == userId)
                .collect(Collectors.toList());
    }

    public List<Rental> getActiveRentalsByUser(int userId) {
        return getAllRentals().stream()
                .filter(rental -> rental.getUserId() == userId && !rental.isReturned())
                .collect(Collectors.toList());
    }

    public List<Rental> getRentalHistoryByUser(int userId) {
        return getAllRentals().stream()
                .filter(rental -> rental.getUserId() == userId && rental.isReturned())
                .collect(Collectors.toList());
    }

    public Rental getActiveRentalByUserAndMovie(int userId, int movieId) {
        return getAllRentals().stream()
                .filter(rental -> rental.getUserId() == userId
                        && rental.getMovieId() == movieId
                        && !rental.isReturned())
                .findFirst()
                .orElse(null);
    }

    public List<Rental> getAllRentals() {
        List<Rental> rentals = new ArrayList<>();
        for (String line : fileHandler.readFromFile(RENTALS_FILE)) {
            try {
                rentals.add(Rental.fromString(line));
            } catch (RuntimeException ignored) {
                // Skip malformed rows and continue loading valid rentals.
            }
        }
        return rentals;
    }

    public boolean returnMovie(int rentalId) {
        List<Rental> rentals = getAllRentals();
        for (int index = 0; index < rentals.size(); index++) {
            Rental rental = rentals.get(index);
            if (rental.getId() != rentalId) {
                continue;
            }
            if (rental.isReturned()) {
                return false;
            }

            rental.setReturned(true);
            rental.setReturnDate(LocalDateTime.now());
            if (rental.getReturnDate().isAfter(rental.getDueDate())) {
                long daysLate = Math.max(1, ChronoUnit.DAYS.between(rental.getDueDate(), rental.getReturnDate()));
                rental.setLateFee(daysLate * LATE_FEE_PER_DAY);
            }

            rentals.set(index, rental);
            fileHandler.writeToFile(RENTALS_FILE, rentals, false);
            movieService.updateAvailableCopies(rental.getMovieId(), 1);
            return true;
        }

        return false;
    }

    public double calculateTotalRentalFee(int userId) {
        return getRentalsByUser(userId).stream()
                .mapToDouble(rental -> rental.getRentalFee() + rental.getLateFee())
                .sum();
    }

    public int getActiveRentalsCount() {
        return (int) getAllRentals().stream()
                .filter(rental -> !rental.isReturned())
                .count();
    }

    public List<Rental> getOverdueRentals() {
        LocalDateTime now = LocalDateTime.now();
        return getAllRentals().stream()
                .filter(rental -> !rental.isReturned() && rental.getDueDate().isBefore(now))
                .collect(Collectors.toList());
    }
}
