package com.movierental.util;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {

    public boolean isValid(String password) {
        return password != null && !password.trim().isEmpty();
    }

    public String getRequirements() {
        return "Password is required.";
    }
}
