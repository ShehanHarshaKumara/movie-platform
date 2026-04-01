package com.movierental.repository;

import com.movierental.model.Movie;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileHandlerTest {

    @Test
    void readFromFileKeepsQuotedMultilineMovieRowsTogether() throws IOException {
        FileHandler fileHandler = new FileHandler();
        String filename = "movie-record-" + UUID.randomUUID() + ".txt";
        Path filePath = Paths.get("data").resolve(filename);

        String multilineMovieRecord = "5,Garfield,Peter Hewitt,Comedy,2004-02-11,2.00,2,2,\"Line one" +
                System.lineSeparator() +
                "Line two\",https://example.com/garfield,/uploads/posters/garfield.jpg";

        try {
            Files.write(filePath, List.of(multilineMovieRecord), StandardCharsets.UTF_8);

            List<String> records = fileHandler.readFromFile(filename);
            Movie movie = Movie.fromString(records.get(0));

            assertEquals(1, records.size());
            assertTrue(movie.getDescription().contains("Line one"));
            assertTrue(movie.getDescription().contains("Line two"));
            assertEquals("/uploads/posters/garfield.jpg", movie.getImagePath());
        } finally {
            Files.deleteIfExists(filePath);
        }
    }
}
