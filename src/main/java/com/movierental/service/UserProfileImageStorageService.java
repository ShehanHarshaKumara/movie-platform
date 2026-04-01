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
public class UserProfileImageStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final String PROFILE_PREFIX = "/uploads/profiles/";

    private final Path uploadsRoot;
    private final Path profileDirectory;

    public UserProfileImageStorageService(@Value("${app.upload-root:data/uploads}") String uploadRoot) {
        this.uploadsRoot = Paths.get(uploadRoot).toAbsolutePath().normalize();
        this.profileDirectory = uploadsRoot.resolve("profiles").normalize();

        try {
            Files.createDirectories(profileDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create profile upload directory.", exception);
        }
    }

    public String storeProfileImage(MultipartFile profileImage, String username, String existingImagePath) {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new IllegalArgumentException("Please choose an image to upload.");
        }

        String extension = extractAllowedExtension(profileImage.getOriginalFilename());
        String filename = buildFilename(username, extension);
        Path targetPath = profileDirectory.resolve(filename).normalize();
        if (!targetPath.startsWith(profileDirectory)) {
            throw new IllegalStateException("Invalid profile image path.");
        }

        try (InputStream inputStream = profileImage.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save the profile image.", exception);
        }

        deleteProfileImage(existingImagePath);
        return PROFILE_PREFIX + filename;
    }

    public void deleteProfileImage(String imagePath) {
        if (!isManagedProfileImage(imagePath)) {
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
            // Stale profile images should not block profile updates.
        }
    }

    private String extractAllowedExtension(String originalFilename) {
        String filename = safeTrim(originalFilename);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new IllegalArgumentException("Profile image must be a JPG, PNG, GIF, or WEBP file.");
        }

        String extension = filename.substring(dotIndex).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Profile image must be a JPG, PNG, GIF, or WEBP file.");
        }
        return extension;
    }

    private String buildFilename(String username, String extension) {
        String baseName = slugify(username);
        if (baseName.isBlank()) {
            baseName = "user";
        }
        return baseName + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    private String slugify(String value) {
        return Normalizer.normalize(safeTrim(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private boolean isManagedProfileImage(String imagePath) {
        return imagePath != null && imagePath.startsWith(PROFILE_PREFIX);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
