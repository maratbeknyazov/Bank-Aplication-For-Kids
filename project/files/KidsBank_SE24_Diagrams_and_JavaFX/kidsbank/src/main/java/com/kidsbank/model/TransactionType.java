package com.kidsbank.model;

/** Types of financial transactions. Group SE-24 */
public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TASK_REWARD("Task Reward"),
    SAVINGS_TRANSFER("Savings Transfer");

    private final String displayName;

    TransactionType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
