package com.kidsbank.model;

/**
 * STAGE 1: Model — enum for account types.
 * Values: CURRENT, SAVINGS.
 * Group SE-24 - Virtual Bank for Kids
 */
public enum AccountType {
    CURRENT("Current Account"),
    SAVINGS("Savings Account");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
