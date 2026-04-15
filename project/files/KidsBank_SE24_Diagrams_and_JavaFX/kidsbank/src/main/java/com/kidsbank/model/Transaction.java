package com.kidsbank.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Records a single financial transaction on an account.
 * Group SE-24 - Virtual Bank for Kids
 */
public class Transaction {
    private String transactionId;
    private String accountId;
    private TransactionType type;
    private double amount;
    private double balanceAfter;
    private String description;
    private LocalDateTime dateTime;

    /** Constructor for new transactions. */
    public Transaction(String accountId, TransactionType type,
                       double amount, double balanceAfter, String description) {
        this.transactionId = UUID.randomUUID().toString();
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.dateTime = LocalDateTime.now();
    }

    /** Constructor for loading from file. */
    public Transaction(String transactionId, String accountId, TransactionType type,
                       double amount, double balanceAfter, String description, LocalDateTime dateTime) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.dateTime = dateTime;
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getAccountId() { return accountId; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public LocalDateTime getDateTime() { return dateTime; }

    /** Returns a signed amount string (+/-). */
    public String getSignedAmount() {
        boolean isCredit = type == TransactionType.DEPOSIT || type == TransactionType.TASK_REWARD;
        return (isCredit ? "+" : "-") + String.format("$%.2f", amount);
    }
}
