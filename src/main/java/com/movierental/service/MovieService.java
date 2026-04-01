package com.movierental.service;

import com.movierental.model.Movie;
import com.movierental.repository.FileHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private static final String MOVIES_FILE = "movies.txt";

    private final FileHandler fileHandler;

    public MovieService(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    public String addMovie(Movie movie) {
        String validation = validateMovieSubmission(movie);
        if (!"success".equals(validation)) {
            return validation;
        }

        movie.setId(fileHandler.getNextId(MOVIES_FILE));
        normalizeNewMovie(movie);
        fileHandler.writeToFile(MOVIES_FILE, List.of(movie), true);
        return "success";
    }

    public Movie getMovieById(int id) {
        return getAllMovies().stream()
                .filter(movie -> movie.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Movie> searchMovies(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllMovies();
        }

        String normalizedKeyword = keyword.trim().toLowerCase();
        return getAllMovies().stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(normalizedKeyword)
                        || movie.getDirector().toLowerCase().contains(normalizedKeyword)
                        || movie.getGenre().toLowerCase().contains(normalizedKeyword))
                .collect(Collectors.toList());
    }

    public List<Movie> getMoviesByGenre(String genre) {
        if (genre == null || genre.isBlank()) {
            return getAllMovies();
        }

        return getAllMovies().stream()
                .filter(movie -> genre.equalsIgnoreCase(movie.getGenre()))
                .collect(Collectors.toList());
    }

    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        for (String line : fileHandler.readFromFile(MOVIES_FILE)) {
            try {
                movies.add(Movie.fromString(line));
            } catch (RuntimeException ignored) {
                // Skip malformed rows so valid movies still load.
            }
        }
        return movies;
    }

    public Map<Integer, Movie> getMovieLookup() {
        Map<Integer, Movie> lookup = new LinkedHashMap<>();
        for (Movie movie : getAllMovies()) {
            lookup.put(movie.getId(), movie);
        }
        return lookup;
    }

    public String updateMovie(Movie updatedMovie) {
        String validation = validateMovieSubmission(updatedMovie);
        if (!"success".equals(validation)) {
            return validation;
        }

        List<Movie> movies = getAllMovies();
        for (int index = 0; index < movies.size(); index++) {
            Movie existingMovie = movies.get(index);
            if (existingMovie.getId() == updatedMovie.getId()) {
                normalizeUpdatedMovie(existingMovie, updatedMovie);
                movies.set(index, updatedMovie);
                persistMovies(movies);
                return "success";
            }
        }

        return "Movie not found.";
    }

    public boolean deleteMovie(int id) {
        List<Movie> movies = getAllMovies();
        boolean removed = movies.removeIf(movie -> movie.getId() == id);
        if (removed) {
            persistMovies(movies);
        }
        return removed;
    }

    public boolean isAvailable(int movieId) {
        Movie movie = getMovieById(movieId);
        return movie != null && movie.getAvailableCopies() > 0;
    }

    public boolean updateAvailableCopies(int movieId, int change) {
        List<Movie> movies = getAllMovies();
        for (Movie movie : movies) {
            if (movie.getId() == movieId) {
                int updatedAvailable = movie.getAvailableCopies() + change;
                if (updatedAvailable < 0 || updatedAvailable > movie.getTotalCopies()) {
                    return false;
                }
                movie.setAvailableCopies(updatedAvailable);
                persistMovies(movies);
                return true;
            }
        }
        return false;
    }

    public void ensureSampleMovies() {
        if (!getAllMovies().isEmpty()) {
            return;
        }

        addMovie(buildMovie("Inception", "Christopher Nolan", "Sci-Fi", LocalDate.of(2010, 7, 16), 3.99, 5,
                "A mind-bending thriller about dream invasion."));
        addMovie(buildMovie("The Dark Knight", "Christopher Nolan", "Action", LocalDate.of(2008, 7, 18), 4.99, 4,
                "Batman faces the Joker in Gotham City."));
        addMovie(buildMovie("The Matrix", "The Wachowskis", "Sci-Fi", LocalDate.of(1999, 3, 31), 3.49, 3,
                "A hacker discovers the truth behind his reality."));
        addMovie(buildMovie("Forrest Gump", "Robert Zemeckis", "Drama", LocalDate.of(1994, 7, 6), 2.99, 6,
                "A warm-hearted journey through memorable moments in history."));
    }

    private Movie buildMovie(String title,
                             String director,
                             String genre,
                             LocalDate releaseDate,
                             double rentalPrice,
                             int copies,
                             String description) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDirector(director);
        movie.setGenre(genre);
        movie.setReleaseDate(releaseDate);
        movie.setRentalPrice(rentalPrice);
        movie.setTotalCopies(copies);
        movie.setAvailableCopies(copies);
        movie.setDescription(description);
        movie.setDownloadLink("");
        movie.setImagePath("");
        return movie;
    }

    public String validateMovieSubmission(Movie movie) {
        if (movie == null) {
            return "Movie details are missing.";
        }
        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            return "Movie title is required.";
        }
        if (movie.getDirector() == null || movie.getDirector().isBlank()) {
            return "Director name is required.";
        }
        if (movie.getGenre() == null || movie.getGenre().isBlank()) {
            return "Genre is required.";
        }
        if (movie.getReleaseDate() == null) {
            return "Release date is required.";
        }
        if (movie.getRentalPrice() <= 0) {
            return "Rental price must be greater than zero.";
        }
        if (movie.getTotalCopies() <= 0 && movie.getAvailableCopies() <= 0) {
            return "Total copies must be at least 1.";
        }
        return "success";
    }

    private void normalizeNewMovie(Movie movie) {
        if (movie.getTotalCopies() <= 0) {
            movie.setTotalCopies(movie.getAvailableCopies());
        }
        if (movie.getAvailableCopies() <= 0) {
            movie.setAvailableCopies(movie.getTotalCopies());
        }
        if (movie.getAvailableCopies() > movie.getTotalCopies()) {
            movie.setAvailableCopies(movie.getTotalCopies());
        }
        if (movie.getDescription() == null) {
            movie.setDescription("");
        }
        movie.setTitle(movie.getTitle().trim());
        movie.setDirector(movie.getDirector().trim());
        movie.setGenre(movie.getGenre().trim());
        movie.setDescription(movie.getDescription().trim());
        movie.setDownloadLink(normalizeDownloadLink(movie.getDownloadLink()));
        movie.setImagePath(safeTrim(movie.getImagePath()));
    }

    private void normalizeUpdatedMovie(Movie existingMovie, Movie updatedMovie) {
        int rentedCopies = Math.max(0, existingMovie.getTotalCopies() - existingMovie.getAvailableCopies());
        int requestedTotal = updatedMovie.getTotalCopies() > 0 ? updatedMovie.getTotalCopies() : existingMovie.getTotalCopies();
        if (requestedTotal < rentedCopies) {
            requestedTotal = rentedCopies;
        }

        updatedMovie.setTotalCopies(requestedTotal);
        updatedMovie.setAvailableCopies(Math.max(0, requestedTotal - rentedCopies));
        if (updatedMovie.getDescription() == null) {
            updatedMovie.setDescription("");
        }
        updatedMovie.setTitle(updatedMovie.getTitle().trim());
        updatedMovie.setDirector(updatedMovie.getDirector().trim());
        updatedMovie.setGenre(updatedMovie.getGenre().trim());
        updatedMovie.setDescription(updatedMovie.getDescription().trim());
        updatedMovie.setDownloadLink(normalizeDownloadLink(updatedMovie.getDownloadLink()));
        updatedMovie.setImagePath(safeTrim(updatedMovie.getImagePath()));
    }

    private void persistMovies(List<Movie> movies) {
        fileHandler.writeToFile(MOVIES_FILE, movies, false);
    }

    private String normalizeDownloadLink(String downloadLink) {
        String normalized = safeTrim(downloadLink);
        if (normalized.isBlank()) {
            return "";
        }
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized;
        }
        return "https://" + normalized;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
