package com.movierental.service;

import com.movierental.model.User;
import com.movierental.repository.FileHandler;
import com.movierental.util.PasswordValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String USERS_FILE = "users.txt";
    private static final String LEGACY_USERS_FILE = "user.txt";

    private final FileHandler fileHandler;
    private final PasswordValidator passwordValidator;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(FileHandler fileHandler, PasswordValidator passwordValidator) {
        this.fileHandler = fileHandler;
        this.passwordValidator = passwordValidator;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String registerUser(User user) {
        if (user == null) {
            return "User details are missing.";
        }
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return "Username is required.";
        }
        if (user.getFullName() == null || user.getFullName().isBlank()) {
            return "Full name is required.";
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return "Email is required.";
        }
        if (!passwordValidator.isValid(user.getPassword())) {
            return passwordValidator.getRequirements();
        }

        List<User> users = getAllUsers();
        if (users.stream().anyMatch(existingUser -> existingUser.getUsername().equalsIgnoreCase(user.getUsername().trim()))) {
            return "Username already exists.";
        }
        if (users.stream().anyMatch(existingUser -> existingUser.getEmail().equalsIgnoreCase(user.getEmail().trim()))) {
            return "Email already exists.";
        }

        user.setId(fileHandler.getNextId(USERS_FILE));
        user.setUsername(user.getUsername().trim());
        user.setFullName(user.getFullName().trim());
        user.setEmail(user.getEmail().trim());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(user.getRole() == null || user.getRole().isBlank() ? "USER" : user.getRole().trim().toUpperCase());
        user.setImagePath(user.getImagePath() == null ? "" : user.getImagePath().trim());
        user.setRegistrationDate(LocalDateTime.now());
        user.setActive(true);

        fileHandler.writeToFile(USERS_FILE, List.of(user), true);
        return "success";
    }

    public User getUserById(int id) {
        return getAllUsers().stream()
                .filter(user -> user.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public User getUserByUsername(String username) {
        if (username == null) {
            return null;
        }

        return getAllUsers().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username.trim()))
                .findFirst()
                .orElse(null);
    }

    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllUsers();
        }

        String normalizedKeyword = keyword.trim().toLowerCase();
        return getAllUsers().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(normalizedKeyword)
                        || user.getFullName().toLowerCase().contains(normalizedKeyword)
                        || user.getEmail().toLowerCase().contains(normalizedKeyword))
                .collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        migrateLegacyUserFile();

        List<User> users = new ArrayList<>();
        for (String line : fileHandler.readFromFile(USERS_FILE)) {
            try {
                users.add(User.fromString(line));
            } catch (RuntimeException ignored) {
                // Ignore malformed rows and continue loading valid users.
            }
        }
        return users;
    }

    public Map<Integer, User> getUserLookup() {
        Map<Integer, User> lookup = new LinkedHashMap<>();
        for (User user : getAllUsers()) {
            lookup.put(user.getId(), user);
        }
        return lookup;
    }

    public boolean updateUser(User updatedUser) {
        List<User> users = getAllUsers();
        for (int index = 0; index < users.size(); index++) {
            User existingUser = users.get(index);
            if (existingUser.getId() == updatedUser.getId()) {
                if (updatedUser.getPassword() == null || updatedUser.getPassword().isBlank()) {
                    updatedUser.setPassword(existingUser.getPassword());
                }
                if (updatedUser.getRegistrationDate() == null) {
                    updatedUser.setRegistrationDate(existingUser.getRegistrationDate());
                }
                if (updatedUser.getRole() == null || updatedUser.getRole().isBlank()) {
                    updatedUser.setRole(existingUser.getRole());
                }
                if (updatedUser.getImagePath() == null || updatedUser.getImagePath().isBlank()) {
                    updatedUser.setImagePath(existingUser.getImagePath());
                }
                users.set(index, updatedUser);
                persistUsers(users);
                return true;
            }
        }
        return false;
    }

    public String updateProfile(int userId, String username, String fullName, String email) {
        User existingUser = getUserById(userId);
        if (existingUser == null) {
            return "User not found.";
        }
        if (username == null || username.isBlank()) {
            return "Username is required.";
        }
        if (fullName == null || fullName.isBlank()) {
            return "Full name is required.";
        }
        if (email == null || email.isBlank()) {
            return "Email is required.";
        }

        for (User user : getAllUsers()) {
            if (user.getId() == userId) {
                continue;
            }
            if (user.getUsername().equalsIgnoreCase(username.trim())) {
                return "Username already exists.";
            }
            if (user.getEmail().equalsIgnoreCase(email.trim())) {
                return "Email already exists.";
            }
        }

        existingUser.setUsername(username.trim());
        existingUser.setFullName(fullName.trim());
        existingUser.setEmail(email.trim());
        updateUser(existingUser);
        return "success";
    }

    public String updateProfileImage(int userId, String imagePath) {
        User existingUser = getUserById(userId);
        if (existingUser == null) {
            return "User not found.";
        }

        existingUser.setImagePath(imagePath == null ? "" : imagePath.trim());
        updateUser(existingUser);
        return "success";
    }

    public String changePassword(int userId,
                                 String currentPassword,
                                 String newPassword,
                                 String confirmPassword) {
        User existingUser = getUserById(userId);
        if (existingUser == null) {
            return "User not found.";
        }
        if (!matchesPassword(currentPassword, existingUser.getPassword())) {
            return "Current password is incorrect.";
        }
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            return "New passwords do not match.";
        }
        if (!passwordValidator.isValid(newPassword)) {
            return passwordValidator.getRequirements();
        }

        existingUser.setPassword(passwordEncoder.encode(newPassword));
        updateUser(existingUser);
        return "success";
    }

    public boolean deleteUser(int id) {
        List<User> users = getAllUsers();
        boolean removed = users.removeIf(user -> user.getId() == id);
        if (removed) {
            persistUsers(users);
        }
        return removed;
    }

    public User authenticate(String username, String password) {
        User user = getUserByUsername(username);
        if (user == null || !user.isActive()) {
            return null;
        }
        if (!matchesPassword(password, user.getPassword())) {
            return null;
        }

        if (!isEncodedPassword(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            updateUser(user);
        }
        return getUserById(user.getId());
    }

    public void ensureAdminUser() {
        boolean adminExists = getAllUsers().stream()
                .anyMatch(user -> "ADMIN".equalsIgnoreCase(user.getRole()));
        if (adminExists) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setFullName("Platform Administrator");
        admin.setEmail("admin@movierental.local");
        admin.setPassword("Admin@123");
        admin.setRole("ADMIN");
        registerUser(admin);
    }

    private void migrateLegacyUserFile() {
        fileHandler.copyIfMissing(LEGACY_USERS_FILE, USERS_FILE);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isEncodedPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private boolean isEncodedPassword(String password) {
        return password != null && password.startsWith("$2");
    }

    private void persistUsers(List<User> users) {
        fileHandler.writeToFile(USERS_FILE, users, false);
    }
}
