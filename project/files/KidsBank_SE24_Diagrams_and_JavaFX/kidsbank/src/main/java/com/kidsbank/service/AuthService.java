package com.kidsbank.service;

import com.kidsbank.model.User;
import com.kidsbank.storage.JsonStorage;

import java.util.Optional;

/**
 * Handles authentication: login, logout, registration, PIN management.
 * Group SE-24 - Virtual Bank for Kids
 */
public class AuthService {

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private User currentUser;

    /**
     * Attempts to log in a user by name, PIN, and role.
     * @return LOGIN_SUCCESS, WRONG_PIN, ACCOUNT_LOCKED, or USER_NOT_FOUND
     */
    public LoginResult login(String name, String pin, String role) {
        Optional<User> opt = JsonStorage.findUserByName(name, role);
        if (opt.isEmpty()) return LoginResult.USER_NOT_FOUND;

        User user = opt.get();

        if (user.isLocked()) return LoginResult.ACCOUNT_LOCKED;

        if (!user.verifyPin(pin)) {
            user.incrementLoginFailures();
            if (user.getLoginFailures() >= MAX_LOGIN_ATTEMPTS) {
                user.setLocked(true);
            }
            JsonStorage.saveUser(user);
            return LoginResult.WRONG_PIN;
        }

        // Successful login
        user.resetLoginFailures();
        JsonStorage.saveUser(user);
        this.currentUser = user;
        return LoginResult.SUCCESS;
    }

    /** Logs out the current user. */
    public void logout() {
        this.currentUser = null;
    }

    /** Returns the currently logged-in user, or null if none. */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Registers a new user (parent creates a child account or parent registers self).
     * @throws IllegalArgumentException if name already exists for role
     */
    public User registerUser(String name, int age, String pin, String role) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be empty.");
        if (pin == null || !pin.matches("\\d{4}"))
            throw new IllegalArgumentException("PIN must be exactly 4 digits.");
        if (JsonStorage.findUserByName(name, role).isPresent())
            throw new IllegalArgumentException("A " + role + " named '" + name + "' already exists.");

        User user = new User(name.trim(), age, pin, role);
        JsonStorage.saveUser(user);
        return user;
    }

    /**
     * Changes the PIN for a user. Only allowed if old PIN matches.
     */
    public boolean changePin(String userId, String oldPin, String newPin) {
        Optional<User> opt = JsonStorage.findUserById(userId);
        if (opt.isEmpty()) return false;
        User user = opt.get();
        if (!user.verifyPin(oldPin)) return false;
        if (newPin == null || !newPin.matches("\\d{4}")) return false;
        user.setPin(newPin);
        user.resetLoginFailures();
        user.setLocked(false);
        JsonStorage.saveUser(user);
        return true;
    }

    /**
     * Resets a child's PIN (parent action — no old PIN required).
     */
    public boolean resetPin(String childUserId, String newPin) {
        Optional<User> opt = JsonStorage.findUserById(childUserId);
        if (opt.isEmpty()) return false;
        User user = opt.get();
        if (newPin == null || !newPin.matches("\\d{4}")) return false;
        user.setPin(newPin);
        user.resetLoginFailures();
        user.setLocked(false);
        JsonStorage.saveUser(user);
        return true;
    }

    /** Login result enum. */
    public enum LoginResult {
        SUCCESS, WRONG_PIN, ACCOUNT_LOCKED, USER_NOT_FOUND
    }
}
