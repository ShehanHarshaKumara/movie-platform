package com.movierental.service;

import com.movierental.model.Movie;
import com.movierental.model.Review;
import com.movierental.repository.FileHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final String REVIEWS_FILE = "reviews.txt";

    private final FileHandler fileHandler;

    public ReviewService(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    public String addReview(Review review) {
        if (review == null) {
            return "Review details are missing.";
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            return "Rating must be between 1 and 5.";
        }
        if (hasUserReviewedMovie(review.getUserId(), review.getMovieId())) {
            return "You already reviewed this movie.";
        }

        review.setId(fileHandler.getNextId(REVIEWS_FILE));
        if (review.getComment() == null) {
            review.setComment("");
        }
        fileHandler.writeToFile(REVIEWS_FILE, List.of(review), true);
        return "success";
    }

    public Review getReviewById(int id) {
        return getAllReviews().stream()
                .filter(review -> review.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Review> getReviewsByMovie(int movieId) {
        return getAllReviews().stream()
                .filter(review -> review.getMovieId() == movieId && review.isApproved())
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsByUser(int userId) {
        return getAllReviews().stream()
                .filter(review -> review.getUserId() == userId)
                .collect(Collectors.toList());
    }

    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        for (String line : fileHandler.readFromFile(REVIEWS_FILE)) {
            try {
                reviews.add(Review.fromString(line));
            } catch (RuntimeException ignored) {
                // Ignore malformed rows.
            }
        }
        return reviews;
    }

    public List<Review> getPendingReviews() {
        return getAllReviews().stream()
                .filter(review -> !review.isApproved())
                .collect(Collectors.toList());
    }

    public boolean updateReview(Review updatedReview) {
        if (updatedReview == null || updatedReview.getRating() < 1 || updatedReview.getRating() > 5) {
            return false;
        }

        List<Review> reviews = getAllReviews();
        for (int index = 0; index < reviews.size(); index++) {
            if (reviews.get(index).getId() == updatedReview.getId()) {
                if (updatedReview.getComment() == null) {
                    updatedReview.setComment("");
                }
                reviews.set(index, updatedReview);
                persistReviews(reviews);
                return true;
            }
        }
        return false;
    }

    public boolean deleteReview(int id) {
        List<Review> reviews = getAllReviews();
        boolean removed = reviews.removeIf(review -> review.getId() == id);
        if (removed) {
            persistReviews(reviews);
        }
        return removed;
    }

    public boolean approveReview(int id) {
        Review review = getReviewById(id);
        if (review == null) {
            return false;
        }
        review.setApproved(true);
        return updateReview(review);
    }

    public boolean hasUserReviewedMovie(int userId, int movieId) {
        return getAllReviews().stream()
                .anyMatch(review -> review.getUserId() == userId && review.getMovieId() == movieId);
    }

    public double getAverageRating(int movieId) {
        List<Review> reviews = getReviewsByMovie(movieId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double total = reviews.stream().mapToInt(Review::getRating).sum();
        return total / reviews.size();
    }

    public List<Movie> sortMoviesByRating(List<Movie> movies) {
        List<Movie> sortedMovies = new ArrayList<>(movies);
        int size = sortedMovies.size();
        for (int outer = 0; outer < size - 1; outer++) {
            for (int inner = 0; inner < size - outer - 1; inner++) {
                double currentRating = getAverageRating(sortedMovies.get(inner).getId());
                double nextRating = getAverageRating(sortedMovies.get(inner + 1).getId());
                if (currentRating < nextRating) {
                    Movie temporaryMovie = sortedMovies.get(inner);
                    sortedMovies.set(inner, sortedMovies.get(inner + 1));
                    sortedMovies.set(inner + 1, temporaryMovie);
                }
            }
        }
        return sortedMovies;
    }

    private void persistReviews(List<Review> reviews) {
        fileHandler.writeToFile(REVIEWS_FILE, reviews, false);
    }
}
