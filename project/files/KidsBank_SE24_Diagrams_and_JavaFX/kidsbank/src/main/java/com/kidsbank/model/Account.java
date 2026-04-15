package com.kidsbank.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a bank account (Current or Savings) belonging to a child.
 * Group SE-24 - Virtual Bank for Kids
 */
public class Account {
    private String accountId;
    private String userId;
    private AccountType accountType;
    private double balance;
    private LocalDate createdDate;

    /** Constructor for creating a new account. */
    public Account(String userId, AccountType accountType) {
        this.accountId = UUID.randomUUID().toString();
        this.userId = userId;
        this.accountType = accountType;
        this.balance = 0.0;
        this.createdDate = LocalDate.now();
    }

    /** Constructor for loading from file. */
    public Account(String accountId, String userId, AccountType accountType,
                   double balance, LocalDate createdDate) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountType = accountType;
        this.balance = balance;
        this.createdDate = createdDate;
    }

    /**
     * Deposits money into the account.
     * @param amount must be greater than 0
     */
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        this.balance += amount;
    }

    /**
     * Withdraws money from the account.
     * @return true if successful, false if insufficient funds
     */
    public boolean withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (amount > balance) return false;
        this.balance -= amount;
        return true;
    }

    // Getters & setters
    public String getAccountId() { return accountId; }
    public String getUserId() { return userId; }
    public AccountType getAccountType() { return accountType; }
    public double getBalance() { return balance; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setBalance(double balance) { this.balance = balance; }

    @Override
    public String toString() {
        return accountType.getDisplayName() + " ($" + String.format("%.2f", balance) + ")";
    }
}
