package com.movierental.model;

import com.movierental.util.StorageCodec;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String director;
    private String genre;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate releaseDate;
    private double rentalPrice;
    private int availableCopies;
    private int totalCopies;
    private String description;
    private String downloadLink;
    private String imagePath;

    public Movie() {
    }

    public Movie(int id, String title, String director, String genre,
                 LocalDate releaseDate, double rentalPrice, int copies, String description) {
        this.id = id;
        this.title = title;
        this.director = director;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.rentalPrice = rentalPrice;
        this.availableCopies = copies;
        this.totalCopies = copies;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(double rentalPrice) {
        this.rentalPrice = rentalPrice;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return StorageCodec.toCsv(
                id,
                title,
                director,
                genre,
                releaseDate,
                rentalPrice,
                availableCopies,
                totalCopies,
                description,
                downloadLink,
                imagePath
        );
    }

    public static Movie fromString(String line) {
        List<String> fields = StorageCodec.parseCsv(line);
        if (fields.size() < 6) {
            throw new IllegalArgumentException("Invalid movie record: " + line);
        }

        Movie movie = new Movie();
        movie.setId(Integer.parseInt(fields.get(0)));
        movie.setTitle(fields.get(1));
        movie.setDirector(fields.get(2));
        movie.setGenre(fields.get(3));
        movie.setReleaseDate(LocalDate.parse(fields.get(4)));
        movie.setRentalPrice(Double.parseDouble(fields.get(5)));
        int availableCopies = parseInt(getField(fields, 6, null), 0);
        int totalCopies = parseInt(getField(fields, 7, null), availableCopies);
        if (availableCopies <= 0 && totalCopies > 0) {
            availableCopies = totalCopies;
        }
        if (totalCopies <= 0 && availableCopies > 0) {
            totalCopies = availableCopies;
        }
        movie.setAvailableCopies(availableCopies);
        movie.setTotalCopies(totalCopies);
        movie.setDescription(getField(fields, 8, ""));
        movie.setDownloadLink(getField(fields, 9, ""));
        movie.setImagePath(getField(fields, 10, ""));
        return movie;
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

    private static int parseInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }
}
