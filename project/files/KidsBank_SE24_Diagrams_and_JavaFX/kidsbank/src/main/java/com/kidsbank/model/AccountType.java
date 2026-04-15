package com.kidsbank.model;

/**
 * Enum representing the type of bank account.
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
