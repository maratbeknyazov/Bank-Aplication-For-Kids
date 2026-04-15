package com.kidsbank.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a savings goal set by a child.
 * Group SE-24 - Virtual Bank for Kids
 */
public class SavingsGoal {
    private String goalId;
    private String accountId;
    private String goalName;
    private double targetAmount;
    private double savedAmount;
    private LocalDate targetDate;  // nullable
    private boolean completed;

    /** Constructor for new goals. */
    public SavingsGoal(String accountId, String goalName, double targetAmount, LocalDate targetDate) {
        this.goalId = UUID.randomUUID().toString();
        this.accountId = accountId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.savedAmount = 0.0;
        this.targetDate = targetDate;
        this.completed = false;
    }

    /** Constructor for loading from file. */
    public SavingsGoal(String goalId, String accountId, String goalName,
                       double targetAmount, double savedAmount,
                       LocalDate targetDate, boolean completed) {
        this.goalId = goalId;
        this.accountId = accountId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.targetDate = targetDate;
        this.completed = completed;
    }

    /**
     * Adds money to this goal.
     * @param amount amount to add
     */
    public void addToSavings(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        savedAmount += amount;
        if (savedAmount >= targetAmount) {
            completed = true;
            savedAmount = targetAmount; // cap at target
        }
    }

    /** Returns progress as a value between 0.0 and 1.0. */
    public double getProgressPercent() {
        if (targetAmount == 0) return 0;
        return Math.min(savedAmount / targetAmount, 1.0);
    }

    // Getters
    public String getGoalId() { return goalId; }
    public String getAccountId() { return accountId; }
    public String getGoalName() { return goalName; }
    public double getTargetAmount() { return targetAmount; }
    public double getSavedAmount() { return savedAmount; }
    public LocalDate getTargetDate() { return targetDate; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public String toString() {
        return goalName + " ($" + String.format("%.2f", savedAmount)
                + " / $" + String.format("%.2f", targetAmount) + ")";
    }
}
