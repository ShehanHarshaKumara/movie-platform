package com.movierental.model;

import com.movierental.util.StorageCodec;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Rental implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private int movieId;
    private LocalDateTime rentalDate;
    private LocalDateTime returnDate;
    private LocalDateTime dueDate;
    private double rentalFee;
    private boolean returned;
    private double lateFee;

    public Rental() {
        this.rentalDate = LocalDateTime.now();
        this.returned = false;
        this.lateFee = 0.0;
    }

    public Rental(int id, int userId, int movieId, LocalDateTime dueDate, double rentalFee) {
        this();
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.dueDate = dueDate;
        this.rentalFee = rentalFee;
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

    public LocalDateTime getRentalDate() {
        return rentalDate;
    }

    public void setRentalDate(LocalDateTime rentalDate) {
        this.rentalDate = rentalDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public double getRentalFee() {
        return rentalFee;
    }

    public void setRentalFee(double rentalFee) {
        this.rentalFee = rentalFee;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    public double getLateFee() {
        return lateFee;
    }

    public void setLateFee(double lateFee) {
        this.lateFee = lateFee;
    }

    @Override
    public String toString() {
        return StorageCodec.toCsv(
                id,
                userId,
                movieId,
                rentalDate,
                returnDate == null ? "" : returnDate,
                dueDate,
                rentalFee,
                returned,
                lateFee
        );
    }

    public static Rental fromString(String line) {
        List<String> fields = StorageCodec.parseCsv(line);
        if (fields.size() < 7) {
            throw new IllegalArgumentException("Invalid rental record: " + line);
        }

        Rental rental = new Rental();
        rental.setId(Integer.parseInt(fields.get(0)));
        rental.setUserId(Integer.parseInt(fields.get(1)));
        rental.setMovieId(Integer.parseInt(fields.get(2)));
        rental.setRentalDate(LocalDateTime.parse(fields.get(3)));
        rental.setReturnDate(parseNullableDateTime(fields.get(4)));
        rental.setDueDate(LocalDateTime.parse(fields.get(5)));
        rental.setRentalFee(Double.parseDouble(fields.get(6)));
        rental.setReturned(Boolean.parseBoolean(getField(fields, 7,
                rental.getReturnDate() == null ? "false" : "true")));
        rental.setLateFee(parseDouble(getField(fields, 8, "0.0")));
        return rental;
    }

    private static String getField(List<String> fields, int index, String defaultValue) {
        if (index >= fields.size()) {
            return defaultValue;
        }

        String value = fields.get(index);
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return defaultValue;
        }
        return value;
    }

    private static LocalDateTime parseNullableDateTime(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private static double parseDouble(String value) {
        if (value == null) {
            return 0.0;
        }
        return Double.parseDouble(value);
    }
}
