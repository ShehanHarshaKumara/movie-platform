package com.movierental.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class MoviePosterStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final String POSTER_PREFIX = "/uploads/posters/";

    private final Path uploadsRoot;
    private final Path posterDirectory;

    public MoviePosterStorageService(@Value("${app.upload-root:data/uploads}") String uploadRoot) {
        this.uploadsRoot = Paths.get(uploadRoot).toAbsolutePath().normalize();
        this.posterDirectory = uploadsRoot.resolve("posters").normalize();

        try {
            Files.createDirectories(posterDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create poster upload directory.", exception);
        }
    }

    public String storePoster(MultipartFile posterImage, String movieTitle, String existingImagePath) {
        String currentPath = safeTrim(existingImagePath);
        if (posterImage == null || posterImage.isEmpty()) {
            return currentPath;
        }

        String extension = extractAllowedExtension(posterImage.getOriginalFilename());
        String filename = buildFilename(movieTitle, extension);
        Path targetPath = posterDirectory.resolve(filename).normalize();
        if (!targetPath.startsWith(posterDirectory)) {
            throw new IllegalStateException("Invalid poster file path.");
        }

        try (InputStream inputStream = posterImage.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save the poster image.", exception);
        }

        deletePoster(currentPath);
        return POSTER_PREFIX + filename;
    }

    public void deletePoster(String imagePath) {
        if (!isManagedPoster(imagePath)) {
            return;
        }

        String relativePath = imagePath.substring("/uploads/".length());
        Path targetPath = uploadsRoot.resolve(relativePath.replace('/', java.io.File.separatorChar)).normalize();
        if (!targetPath.startsWith(uploadsRoot)) {
            return;
        }

        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException ignored) {
            // A stale poster file should not block the movie workflow.
        }
    }

    private String extractAllowedExtension(String originalFilename) {
        String filename = safeTrim(originalFilename);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new IllegalArgumentException("Poster image must be a JPG, PNG, GIF, or WEBP file.");
        }

        String extension = filename.substring(dotIndex).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Poster image must be a JPG, PNG, GIF, or WEBP file.");
        }
        return extension;
    }

    private String buildFilename(String movieTitle, String extension) {
        String baseName = slugify(movieTitle);
        if (baseName.isBlank()) {
            baseName = "movie";
        }
        return baseName + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(safeTrim(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized;
    }

    private boolean isManagedPoster(String imagePath) {
        return imagePath != null && imagePath.startsWith(POSTER_PREFIX);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
