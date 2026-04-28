package com.kidsbank.model;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * STAGE 1: Model — data structure for users (child/parent).
 * Stores: name, age, PIN hash, role. No business logic.
 * Group SE-24 - Virtual Bank for Kids
 */
public class User {
    private String userId;
    private String name;
    private int age;
    private String pinHash;
    private String role; // "child" or "parent"
    private int loginFailures;
    private boolean locked;

    /** Constructor for creating a new user. */
    public User(String name, int age, String pin, String role) {
        this.userId = UUID.randomUUID().toString();
        this.name = name;
        this.age = age;
        this.pinHash = hashPin(pin);
        this.role = role;
        this.loginFailures = 0;
        this.locked = false;
    }

    /** Constructor for loading from file (all fields provided). */
    public User(String userId, String name, int age, String pinHash, String role,
                int loginFailures, boolean locked) {
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.pinHash = pinHash;
        this.role = role;
        this.loginFailures = loginFailures;
        this.locked = locked;
    }

    /** Verifies the given PIN against the stored hash. */
    public boolean verifyPin(String pin) {
        return pinHash.equals(hashPin(pin));
    }

    /** Updates the user's PIN. */
    public void setPin(String newPin) {
        this.pinHash = hashPin(newPin);
    }

    /** SHA-256 hashes the PIN for secure storage. */
    private String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing PIN", e);
        }
    }

    public void incrementLoginFailures() { loginFailures++; }
    public void resetLoginFailures() { loginFailures = 0; }
    public void setLocked(boolean locked) { this.locked = locked; }

    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getPinHash() { return pinHash; }
    public String getRole() { return role; }
    public int getLoginFailures() { return loginFailures; }
    public boolean isLocked() { return locked; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name=" + name + ", role=" + role + "}";
    }
}
