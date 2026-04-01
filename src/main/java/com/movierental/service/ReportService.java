package com.movierental.service;

import com.movierental.model.Movie;
import com.movierental.model.Rental;
import com.movierental.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final RentalService rentalService;
    private final MovieService movieService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final RecentlyWatchedService recentlyWatchedService;

    public ReportService(RentalService rentalService,
                         MovieService movieService,
                         UserService userService,
                         ReviewService reviewService,
                         RecentlyWatchedService recentlyWatchedService) {
        this.rentalService = rentalService;
        this.movieService = movieService;
        this.userService = userService;
        this.reviewService = reviewService;
        this.recentlyWatchedService = recentlyWatchedService;
    }

    public static class Report {
        private LocalDate generatedDate;
        private int totalRentals;
        private int activeRentals;
        private double totalRevenue;
        private int totalUsers;
        private int totalMovies;
        private int totalReviews;
        private List<Movie> topRatedMovies;
        private List<Movie> mostWatchedMovies;
        private Map<String, Integer> rentalsByGenre;
        private List<User> activeUsers;
        private List<Rental> overdueRentals;

        public LocalDate getGeneratedDate() {
            return generatedDate;
        }

        public void setGeneratedDate(LocalDate generatedDate) {
            this.generatedDate = generatedDate;
        }

        public int getTotalRentals() {
            return totalRentals;
        }

        public void setTotalRentals(int totalRentals) {
            this.totalRentals = totalRentals;
        }

        public int getActiveRentals() {
            return activeRentals;
        }

        public void setActiveRentals(int activeRentals) {
            this.activeRentals = activeRentals;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public int getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(int totalUsers) {
            this.totalUsers = totalUsers;
        }

        public int getTotalMovies() {
            return totalMovies;
        }

        public void setTotalMovies(int totalMovies) {
            this.totalMovies = totalMovies;
        }

        public int getTotalReviews() {
            return totalReviews;
        }

        public void setTotalReviews(int totalReviews) {
            this.totalReviews = totalReviews;
        }

        public List<Movie> getTopRatedMovies() {
            return topRatedMovies;
        }

        public void setTopRatedMovies(List<Movie> topRatedMovies) {
            this.topRatedMovies = topRatedMovies;
        }

        public List<Movie> getMostWatchedMovies() {
            return mostWatchedMovies;
        }

        public void setMostWatchedMovies(List<Movie> mostWatchedMovies) {
            this.mostWatchedMovies = mostWatchedMovies;
        }

        public Map<String, Integer> getRentalsByGenre() {
            return rentalsByGenre;
        }

        public void setRentalsByGenre(Map<String, Integer> rentalsByGenre) {
            this.rentalsByGenre = rentalsByGenre;
        }

        public List<User> getActiveUsers() {
            return activeUsers;
        }

        public void setActiveUsers(List<User> activeUsers) {
            this.activeUsers = activeUsers;
        }

        public List<Rental> getOverdueRentals() {
            return overdueRentals;
        }

        public void setOverdueRentals(List<Rental> overdueRentals) {
            this.overdueRentals = overdueRentals;
        }
    }

    public Report generateReport() {
        Report report = new Report();
        List<Rental> allRentals = rentalService.getAllRentals();
        report.setGeneratedDate(LocalDate.now());
        report.setTotalRentals(allRentals.size());
        report.setActiveRentals((int) allRentals.stream().filter(rental -> !rental.isReturned()).count());
        report.setTotalRevenue(calculateTotalRevenue());
        report.setTotalUsers(userService.getAllUsers().size());
        report.setTotalMovies(movieService.getAllMovies().size());
        report.setTotalReviews(reviewService.getAllReviews().size());
        report.setTopRatedMovies(getTopRatedMovies(5));
        report.setMostWatchedMovies(getMostWatchedMovies(5));
        report.setRentalsByGenre(getRentalsByGenre());
        report.setActiveUsers(getMostActiveUsers(5));
        report.setOverdueRentals(rentalService.getOverdueRentals());
        return report;
    }

    public List<Movie> getTopRatedMovies(int limit) {
        List<Movie> movies = reviewService.sortMoviesByRating(movieService.getAllMovies());
        return movies.stream().limit(limit).collect(Collectors.toList());
    }

    public List<Movie> getMostWatchedMovies(int limit) {
        return movieService.getAllMovies().stream()
                .sorted((left, right) -> Integer.compare(
                        recentlyWatchedService.getWatchCount(right.getId()),
                        recentlyWatchedService.getWatchCount(left.getId())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Double> getMonthlyRevenue(int year) {
        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String month : months) {
            monthlyRevenue.put(month, 0.0);
        }

        for (Rental rental : rentalService.getAllRentals()) {
            if (rental.getRentalDate().getYear() != year) {
                continue;
            }
            String month = months[rental.getRentalDate().getMonthValue() - 1];
            monthlyRevenue.merge(month, rental.getRentalFee() + rental.getLateFee(), Double::sum);
        }
        return monthlyRevenue;
    }

    public Map<String, Object> getUserReport(int userId) {
        Map<String, Object> report = new HashMap<>();
        User user = userService.getUserById(userId);
        if (user == null) {
            return report;
        }

        report.put("user", user);
        report.put("rentals", rentalService.getRentalsByUser(userId));
        report.put("totalRentals", rentalService.getRentalsByUser(userId).size());
        report.put("activeRentals", rentalService.getActiveRentalsByUser(userId).size());
        report.put("totalSpent", rentalService.calculateTotalRentalFee(userId));
        report.put("totalReviews", reviewService.getReviewsByUser(userId).size());
        report.put("recentlyWatched", recentlyWatchedService.getRecentlyWatchedList(userId));
        return report;
    }

    private double calculateTotalRevenue() {
        return rentalService.getAllRentals().stream()
                .mapToDouble(rental -> rental.getRentalFee() + rental.getLateFee())
                .sum();
    }

    private Map<String, Integer> getRentalsByGenre() {
        Map<String, Integer> rentalsByGenre = new LinkedHashMap<>();
        for (Rental rental : rentalService.getAllRentals()) {
            Movie movie = movieService.getMovieById(rental.getMovieId());
            if (movie != null) {
                rentalsByGenre.merge(movie.getGenre(), 1, Integer::sum);
            }
        }
        return rentalsByGenre;
    }

    private List<User> getMostActiveUsers(int limit) {
        return userService.getAllUsers().stream()
                .sorted((left, right) -> Integer.compare(
                        rentalService.getRentalsByUser(right.getId()).size(),
                        rentalService.getRentalsByUser(left.getId()).size()))
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
