package com.movierental.repository;

import com.movierental.util.StorageCodec;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileHandler {

    private static final String DATA_DIRECTORY = "data";

    private final Path dataDirectory;

    public FileHandler() {
        this.dataDirectory = Paths.get(DATA_DIRECTORY);
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create data directory.", exception);
        }
    }

    public synchronized <T> void writeToFile(String filename, List<T> data, boolean append) {
        Path filePath = resolve(filename);
        OpenOption[] options = append
                ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND}
                : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8, options)) {
            for (T item : data) {
                writer.write(String.valueOf(item));
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write to " + filename, exception);
        }
    }

    public synchronized List<String> readFromFile(String filename) {
        Path filePath = resolve(filename);
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            List<String> allLines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            List<String> filteredLines = new ArrayList<>();
            StringBuilder currentRecord = new StringBuilder();
            for (String line : allLines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                if (currentRecord.length() > 0) {
                    currentRecord.append(System.lineSeparator());
                }
                currentRecord.append(line);

                if (StorageCodec.isCompleteCsvRecord(currentRecord.toString())) {
                    filteredLines.add(currentRecord.toString().trim());
                    currentRecord.setLength(0);
                }
            }

            if (currentRecord.length() > 0) {
                filteredLines.add(currentRecord.toString().trim());
            }

            return filteredLines;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read from " + filename, exception);
        }
    }

    public synchronized void clearFile(String filename) {
        writeToFile(filename, List.of(), false);
    }

    public synchronized boolean exists(String filename) {
        return Files.exists(resolve(filename));
    }

    public synchronized void copyIfMissing(String sourceFilename, String targetFilename) {
        Path source = resolve(sourceFilename);
        Path target = resolve(targetFilename);
        if (!Files.exists(source) || Files.exists(target)) {
            return;
        }

        try {
            Files.copy(source, target);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to copy legacy data file.", exception);
        }
    }

    public synchronized int getNextId(String filename) {
        int maxId = 0;
        for (String record : readFromFile(filename)) {
            List<String> fields = StorageCodec.parseCsv(record);
            if (fields.isEmpty()) {
                continue;
            }

            try {
                maxId = Math.max(maxId, Integer.parseInt(fields.get(0)));
            } catch (NumberFormatException ignored) {
                // Skip malformed rows and keep scanning the file.
            }
        }
        return maxId + 1;
    }

    private Path resolve(String filename) {
        return dataDirectory.resolve(filename);
    }
}
