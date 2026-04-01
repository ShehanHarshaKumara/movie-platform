package com.movierental.model;

import com.movierental.util.StorageCodec;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private int movieId;
    private String username;
    private String movieTitle;
    private int rating;
    private String comment;
    private LocalDateTime reviewDate;
    private boolean approved;

    public Review() {
        this.reviewDate = LocalDateTime.now();
        this.approved = true;
    }

    public Review(int id, int userId, int movieId, String username, String movieTitle,
                  int rating, String comment) {
        this();
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.username = username;
        this.movieTitle = movieTitle;
        this.rating = rating;
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Override
    public String toString() {
        return StorageCodec.toCsv(
                id,
                userId,
                movieId,
                username,
                movieTitle,
                rating,
                comment,
                reviewDate,
                approved
        );
    }

    public static Review fromString(String line) {
        List<String> fields = StorageCodec.parseCsv(line);
        if (fields.size() < 6) {
            throw new IllegalArgumentException("Invalid review record: " + line);
        }

        Review review = new Review();
        review.setId(Integer.parseInt(fields.get(0)));
        review.setUserId(Integer.parseInt(fields.get(1)));
        review.setMovieId(Integer.parseInt(fields.get(2)));
        review.setUsername(fields.get(3));
        review.setMovieTitle(fields.get(4));
        review.setRating(Integer.parseInt(fields.get(5)));
        review.setComment(extractComment(fields));
        review.setReviewDate(extractReviewDate(fields));
        review.setApproved(Boolean.parseBoolean(extractApprovedFlag(fields)));
        return review;
    }

    private static String extractComment(List<String> fields) {
        if (fields.size() <= 6) {
            return "";
        }

        String candidate = fields.get(6);
        if (candidate == null || candidate.isBlank() || "null".equalsIgnoreCase(candidate)) {
            return "";
        }
        if (fields.size() == 7 && canParseDateTime(candidate)) {
            return "";
        }
        return candidate;
    }

    private static LocalDateTime extractReviewDate(List<String> fields) {
        if (fields.size() <= 6) {
            return LocalDateTime.now();
        }

        String candidate = fields.size() > 7 ? fields.get(7) : fields.get(6);
        if (candidate == null || candidate.isBlank() || "null".equalsIgnoreCase(candidate)) {
            return LocalDateTime.now();
        }
        if (!canParseDateTime(candidate)) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(candidate);
    }

    private static String extractApprovedFlag(List<String> fields) {
        if (fields.size() > 8) {
            return normalizeBoolean(fields.get(8));
        }
        if (fields.size() == 7 && isBoolean(fields.get(6))) {
            return normalizeBoolean(fields.get(6));
        }
        return "true";
    }

    private static boolean canParseDateTime(String value) {
        try {
            LocalDateTime.parse(value);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    private static String normalizeBoolean(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return "true";
        }
        return value;
    }
}
