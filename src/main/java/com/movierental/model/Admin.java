package com.movierental.model;

import com.movierental.util.StorageCodec;

import java.util.List;

public class Admin extends User {
    private String adminLevel;
    private String department;

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(int id, String username, String password, String email,
                 String fullName, String adminLevel, String department) {
        super(id, username, password, email, fullName);
        this.adminLevel = adminLevel;
        this.department = department;
        setRole("ADMIN");
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean canManageUsers() {
        return "SUPER".equalsIgnoreCase(adminLevel) || "SENIOR".equalsIgnoreCase(adminLevel);
    }

    public boolean canManageMovies() {
        return true;
    }

    public boolean canManageReviews() {
        return true;
    }

    public boolean canGenerateReports() {
        return "SUPER".equalsIgnoreCase(adminLevel) || "SENIOR".equalsIgnoreCase(adminLevel);
    }

    @Override
    public String toString() {
        return StorageCodec.toCsv(
                getId(),
                getUsername(),
                getPassword(),
                getEmail(),
                getFullName(),
                getRole(),
                getRegistrationDate(),
                isActive(),
                adminLevel,
                department
        );
    }

    public static Admin fromString(String line) {
        List<String> fields = StorageCodec.parseCsv(line);
        Admin admin = new Admin();
        admin.setId(Integer.parseInt(fields.get(0)));
        admin.setUsername(fields.get(1));
        admin.setPassword(fields.get(2));
        admin.setEmail(fields.get(3));
        admin.setFullName(fields.get(4));
        admin.setRole(fields.get(5));
        admin.setRegistrationDate(java.time.LocalDateTime.parse(fields.get(6)));
        admin.setActive(Boolean.parseBoolean(fields.get(7)));
        admin.setAdminLevel(fields.size() > 8 ? fields.get(8) : "");
        admin.setDepartment(fields.size() > 9 ? fields.get(9) : "");
        return admin;
    }
}
