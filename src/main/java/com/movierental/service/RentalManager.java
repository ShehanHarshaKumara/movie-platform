package com.movierental.service;

import com.movierental.model.Rental;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RentalManager {

    private final RentalService rentalService;

    public RentalManager(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    public String rentMovie(int userId, int movieId, int days) {
        return rentalService.rentMovie(userId, movieId, days);
    }

    public boolean returnMovie(int rentalId) {
        return rentalService.returnMovie(rentalId);
    }

    public List<Rental> trackRentedMovies(int userId) {
        return rentalService.getActiveRentalsByUser(userId);
    }

    public List<Rental> getRentalHistory(int userId) {
        return rentalService.getRentalHistoryByUser(userId);
    }

    public double calculateRentalFee(int movieId, int days) {
        return rentalService.calculateRentalFee(movieId, days);
    }

    public double calculateTotalRentalFee(int userId) {
        return rentalService.calculateTotalRentalFee(userId);
    }

    public Rental getRentalById(int rentalId) {
        return rentalService.getRentalById(rentalId);
    }

    public List<Rental> getAllRentals() {
        return rentalService.getAllRentals();
    }

    public List<Rental> getOverdueRentals() {
        return rentalService.getOverdueRentals();
    }
}
