package com.movierental.model;

import com.movierental.util.StorageCodec;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String role;
    private String imagePath;
    private LocalDateTime registrationDate;
    private boolean active;

    public User() {
        this.registrationDate = LocalDateTime.now();
        this.role = "USER";
        this.active = true;
    }

    public User(int id, String username, String password, String email, String fullName) {
        this();
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return StorageCodec.toCsv(
                id,
                username,
                password,
                email,
                fullName,
                role,
                imagePath,
                registrationDate,
                active
        );
    }

    public static User fromString(String line) {
        List<String> fields = StorageCodec.parseCsv(line);
        if (fields.size() < 5) {
            throw new IllegalArgumentException("Invalid user record: " + line);
        }

        User user = new User();
        user.setId(Integer.parseInt(fields.get(0)));
        user.setUsername(fields.get(1));
        user.setPassword(fields.get(2));
        user.setEmail(fields.get(3));
        user.setFullName(fields.get(4));
        user.setRole(getField(fields, 5, "USER"));
        boolean hasImageColumn = fields.size() >= 9;
        user.setImagePath(hasImageColumn ? getField(fields, 6, "") : "");
        user.setRegistrationDate(parseRegistrationDate(getField(fields, hasImageColumn ? 7 : 6, null)));
        user.setActive(Boolean.parseBoolean(getField(fields, hasImageColumn ? 8 : 7, "true")));
        return user;
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

    private static LocalDateTime parseRegistrationDate(String value) {
        if (value == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(value);
    }
}
