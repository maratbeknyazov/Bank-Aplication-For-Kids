package com.kidsbank.model;

/**
 * STAGE 1: Model — enum for transaction types.
 * Values: DEPOSIT, WITHDRAWAL, TASK_REWARD, SAVINGS_TRANSFER.
 * Group SE-24
 */
public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TASK_REWARD("Task Reward"),
    SAVINGS_TRANSFER("Savings Transfer");

    private final String displayName;

    TransactionType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
