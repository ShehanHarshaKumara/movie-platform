package com.movierental.service;

import com.movierental.model.Movie;
import com.movierental.repository.FileHandler;
import com.movierental.util.StorageCodec;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

@Service
public class RecentlyWatchedService {

    private static final String RECENTLY_WATCHED_FILE = "recently-watched.txt";
    private static final int MAX_HISTORY_SIZE = 10;

    private final FileHandler fileHandler;
    private final MovieService movieService;

    public RecentlyWatchedService(FileHandler fileHandler, MovieService movieService) {
        this.fileHandler = fileHandler;
        this.movieService = movieService;
    }

    private static class WatchedEntry {
        private final int userId;
        private final int movieId;
        private final LocalDateTime watchedTime;

        private WatchedEntry(int userId, int movieId, LocalDateTime watchedTime) {
            this.userId = userId;
            this.movieId = movieId;
            this.watchedTime = watchedTime;
        }

        @Override
        public String toString() {
            return StorageCodec.toCsv(userId, movieId, watchedTime);
        }

        static WatchedEntry fromString(String line) {
            List<String> fields = StorageCodec.parseCsv(line);
            return new WatchedEntry(
                    Integer.parseInt(fields.get(0)),
                    Integer.parseInt(fields.get(1)),
                    LocalDateTime.parse(fields.get(2))
            );
        }
    }

    public void addToRecentlyWatched(int userId, int movieId) {
        if (movieService.getMovieById(movieId) == null) {
            return;
        }

        List<WatchedEntry> entries = getAllWatchedEntries();
        entries.removeIf(entry -> entry.userId == userId && entry.movieId == movieId);
        entries.add(new WatchedEntry(userId, movieId, LocalDateTime.now()));

        List<WatchedEntry> userEntries = entries.stream()
                .filter(entry -> entry.userId == userId)
                .sorted(Comparator.comparing(entry -> entry.watchedTime))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        while (userEntries.size() > MAX_HISTORY_SIZE) {
            WatchedEntry oldestEntry = userEntries.remove(0);
            entries.removeIf(entry -> entry.userId == oldestEntry.userId
                    && entry.movieId == oldestEntry.movieId
                    && entry.watchedTime.equals(oldestEntry.watchedTime));
        }

        saveAllEntries(entries);
    }

    public Stack<Movie> getRecentlyWatched(int userId) {
        List<WatchedEntry> userEntries = getAllWatchedEntries().stream()
                .filter(entry -> entry.userId == userId)
                .sorted(Comparator.comparing(entry -> entry.watchedTime))
                .collect(java.util.stream.Collectors.toList());

        Stack<Movie> movieStack = new Stack<>();
        for (WatchedEntry entry : userEntries) {
            Movie movie = movieService.getMovieById(entry.movieId);
            if (movie != null) {
                movieStack.push(movie);
            }
        }
        return movieStack;
    }

    public List<Movie> getRecentlyWatchedList(int userId) {
        Stack<Movie> stack = getRecentlyWatched(userId);
        List<Movie> movies = new ArrayList<>();
        ListIterator<Movie> iterator = stack.listIterator(stack.size());
        while (iterator.hasPrevious()) {
            movies.add(iterator.previous());
        }
        return movies;
    }

    public boolean clearHistory(int userId) {
        List<WatchedEntry> entries = getAllWatchedEntries();
        entries.removeIf(entry -> entry.userId == userId);
        saveAllEntries(entries);
        return true;
    }

    public boolean removeFromHistory(int userId, int movieId) {
        List<WatchedEntry> entries = getAllWatchedEntries();
        entries.removeIf(entry -> entry.userId == userId && entry.movieId == movieId);
        saveAllEntries(entries);
        return true;
    }

    public int getWatchCount(int movieId) {
        return (int) getAllWatchedEntries().stream()
                .filter(entry -> entry.movieId == movieId)
                .count();
    }

    private List<WatchedEntry> getAllWatchedEntries() {
        List<WatchedEntry> entries = new ArrayList<>();
        for (String line : fileHandler.readFromFile(RECENTLY_WATCHED_FILE)) {
            try {
                entries.add(WatchedEntry.fromString(line));
            } catch (RuntimeException ignored) {
                // Skip malformed rows so valid history still loads.
            }
        }
        return entries;
    }

    private void saveAllEntries(List<WatchedEntry> entries) {
        fileHandler.writeToFile(RECENTLY_WATCHED_FILE, entries, false);
    }
}
