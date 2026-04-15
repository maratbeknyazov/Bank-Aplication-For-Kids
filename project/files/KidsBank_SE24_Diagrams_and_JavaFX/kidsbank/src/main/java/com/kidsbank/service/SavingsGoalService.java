package com.kidsbank.service;

import com.kidsbank.model.SavingsGoal;
import com.kidsbank.storage.JsonStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for savings goals.
 * Group SE-24 - Virtual Bank for Kids
 */
public class SavingsGoalService {

    private final AccountService accountService;

    public SavingsGoalService(AccountService accountService) {
        this.accountService = accountService;
    }

    /** Creates a new savings goal for the given account. */
    public SavingsGoal createGoal(String accountId, String name, double target, LocalDate targetDate) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Goal name cannot be empty.");
        if (target <= 0)
            throw new IllegalArgumentException("Target amount must be greater than zero.");
        SavingsGoal goal = new SavingsGoal(accountId, name.trim(), target, targetDate);
        JsonStorage.saveSavingsGoal(goal);
        return goal;
    }

    /**
     * Transfers money from the account into a savings goal.
     * @throws IllegalArgumentException if insufficient funds
     */
    public SavingsGoal addToGoal(String goalId, double amount, String accountId) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero.");

        SavingsGoal goal = findGoalById(goalId);
        // This will throw if insufficient funds
        accountService.savingsTransfer(accountId, amount, goal.getGoalName());
        goal.addToSavings(amount);
        JsonStorage.saveSavingsGoal(goal);
        return goal;
    }

    /** Returns all goals for an account (active and completed). */
    public List<SavingsGoal> getGoalsForAccount(String accountId) {
        return JsonStorage.findGoalsByAccountId(accountId);
    }

    /** Returns only active (not completed) goals. */
    public List<SavingsGoal> getActiveGoals(String accountId) {
        return getGoalsForAccount(accountId).stream()
                .filter(g -> !g.isCompleted())
                .collect(Collectors.toList());
    }

    /** Returns only completed goals. */
    public List<SavingsGoal> getCompletedGoals(String accountId) {
        return getGoalsForAccount(accountId).stream()
                .filter(SavingsGoal::isCompleted)
                .collect(Collectors.toList());
    }

    /** Finds a goal by ID scanning all stored goals. */
    public SavingsGoal findGoalById(String goalId) {
        return JsonStorage.findAllGoals().stream()
                .filter(g -> g.getGoalId().equals(goalId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
    }
}
